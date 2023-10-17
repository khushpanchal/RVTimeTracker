package com.rvtimetracker

import android.graphics.Rect
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rvtimetracker.observers.TrackerAdapterDataObserver
import com.rvtimetracker.observers.TrackerObserver

/**
 * RVTimeTracker Library Overview:
 * The RVTimeTracker library is designed to track the time spent by the user viewing items in a RecyclerView in an Android application.
 *
 * Initialization and Configuration:
 * The library is initialized using the "init" function, which creates an instance of the "RVTimeTracker" class.
 *
 * To Initialize the library write the below code after creating recycler view
 *
 * RVTimeTracker.init (
 *     recyclerView = binding.recyclerView,
 *     minTimeInMs = 1000,
 *     minHeightInRatio = 0.45,
 *     minWidthInRatio = 0.45,
 *     dataLimit = 8,
 *     trackItem = { trackInfo ->
 *         Log.i("TrackData", trackInfo.toString())
 *     },
 *     trackAll = { trackInfoList ->
 *         for (item in trackInfoList) {
 *             Log.i("TrackDataAll", item.toString())
 *         }
 *     }
 * )
 *
 * Important Note:
 * It's crucial to use view tag for each view inside onBindViewHolder. The view tag should be unique for each data item.
 * Library will throw IllegalArgumentException if view does not contain view tag.
 * Library would use this view tag as a key to store the data.
 *
 * For example:
 * data class Name(val id: String, val name: String)
 * val itemList = listOf(
 *     Name("John", UUID.randomUUID().toString()),
 *     Name("Jane", UUID.randomUUID().toString()),
 *     Name("Alice", UUID.randomUUID().toString()),
 *     // Add more data items...
 * )
 *
 * Inside onBindViewHolder:
 * override fun onBindViewHolder(holder: YourViewHolder, position: Int) {
 *     val item = itemList[position]
 *     // Set the unique view tag for each item
 *     holder.itemView.tag = item.id
 *     // Bind other views...
 * }
 */


/**
 * Key Parameters for Initialization:
 * @param recyclerView The RecyclerView to be tracked for item view times.
 * @param minTimeInMs (optional, default = 0): Minimum time in milliseconds a view needs to be visible to be tracked. Value should be greater than 0
 * @param minHeightInRatio (optional, default = 0.5): Minimum height ratio a view should have to be tracked. Value should be between 0 and 1
 * @param minWidthInRatio (optional, default = 0.5): Minimum width ratio a view should have to be tracked. Value should be between 0 and 1
 * @param dataLimit (optional, default = 10): Number of data after which "trackAll" lambda block will be invoked with the list of tracked data. Value should be less than or equal to 50
 * @param trackItem Lambda function to be executed when any item moves out of the visible screen. (Contain TrackInfo)
 * @param trackAll  Lambda function to be executed when the dataLimit is reached or after onStop lifecycle method of the RecyclerView context is called. (Contains List<TrackInfo>)
 * @see TrackInfo
 * @throws IllegalArgumentException when limits not meet or view tag is not present
 */
class RVTimeTracker private constructor(
    private val recyclerView: RecyclerView,
    private val minTimeInMs: Long,
    private val minHeightInRatio: Double,
    private val minWidthInRatio: Double,
    private val dataLimit: Int,
    private val trackItem: ((TrackInfo) -> Unit)?,
    private val trackAll: ((List<TrackInfo>) -> Unit)?
) {
    companion object {
        // Used to initialize the library, creates the instance of RVTimeTracker class
        /**
         * Limits for each parameter passed
         * @param dataLimit should be less than or equal to 50
         * @param minTimeInMs should be greater than equals to 0
         * @param minHeightInRatio should be between 0 and 1 including both
         * @param minWidthInRatio should be between 0 and 1 including both
         */
        fun init(
            recyclerView: RecyclerView,
            minTimeInMs: Long = 0L,
            minHeightInRatio: Double = 0.5,
            minWidthInRatio: Double = 0.5,
            dataLimit: Int = 10,
            trackItem: ((TrackInfo) -> Unit)? = null,
            trackAll: ((List<TrackInfo>) -> Unit)? = null
        ) {
            if (dataLimit > 50 || minTimeInMs < 0 || minHeightInRatio < 0 || minHeightInRatio > 1 || minWidthInRatio < 0 || minWidthInRatio > 1) {
                throw IllegalArgumentException("dataLimit/minTimeInMs/minHeightInRatio/minWidthInRatio value should be within limit")
            }
            RVTimeTracker(
                recyclerView,
                minTimeInMs,
                minHeightInRatio,
                minWidthInRatio,
                dataLimit,
                trackItem,
                trackAll
            )
        }
    }

    // Keeps a list of items visible on the screen when the screen is not scrolling.
    private val currentListMap = mutableMapOf<String, TrackData>()

    // Keeps a list of items visible on the screen just after scrolling.
    private val newListMap = mutableMapOf<String, TrackData>()

    // Keeps a list of items being tracked and sent to the client.
    private val trackInfoMap = mutableMapOf<String, TrackInfo>()
    private var trackOnGlobalLayout = false

    init {

        registerObservers()
        /*
            Listens for scroll events in the RecyclerView and triggers the addition of new items to the list and tracking.
        */
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                addNewListAndTrack()
            }
        })
        /*
            Listens for global layout changes (like initial layout) and triggers the addition of items and tracking.
        */
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!trackOnGlobalLayout) {
                trackOnGlobalLayout = true
                if(!recyclerView.isAnimating) {
                    addNewListAndTrack()
                } else {
                    recyclerView.itemAnimator?.isRunning {
                        //invoked on animation end
                        if(!recyclerView.isAnimating) {
                            addNewListAndTrack()
                        }
                    }
                }
            }
        }
    }

    private fun registerObservers() {
        (recyclerView.context as LifecycleOwner).lifecycle.addObserver(TrackerObserver(this))
        recyclerView.adapter?.registerAdapterDataObserver(TrackerAdapterDataObserver(this))
    }

    private fun addNewListAndTrack() {
        newListMap.clear()
        val firstVisibleItemPosition = (recyclerView.layoutManager as? LinearLayoutManager?)
            ?.findFirstVisibleItemPosition() ?: -1
        val lastVisibleItemPosition = (recyclerView.layoutManager as? LinearLayoutManager?)
            ?.findLastVisibleItemPosition() ?: -1
        for (viewPosition in firstVisibleItemPosition..lastVisibleItemPosition) {
            if (viewPosition < 0) continue
            val itemView = recyclerView.layoutManager?.findViewByPosition(viewPosition)
            if (itemView != null &&
                getVisibleHeightPercentage(itemView) >= minHeightInRatio &&
                getVisibleWidthPercentage(itemView) >= minWidthInRatio) {
                if (itemView.tag?.toString() == null || itemView.tag?.toString()!!.isEmpty()) {
                    throw IllegalArgumentException("view tag is necessary")
                }
                newListMap[itemView.tag.toString()] =
                    TrackData(viewPosition, System.currentTimeMillis())
            }
        }
        compareAndTrack(currentListMap.toMap(), newListMap)
    }

    private fun compareAndTrack(
        currentMap: Map<String, TrackData>,
        newMap: Map<String, TrackData>
    ) {
        currentMap.forEach {
            if (!newMap.containsKey(it.key)) {
                trackItemAndAddToList(it)
                currentListMap.remove(it.key)
            }
        }

        newMap.forEach {
            if (!currentMap.containsKey(it.key)) {
                currentListMap[it.key] = it.value
            }
        }
    }

    private fun getVisibleHeightPercentage(view: View): Double {
        val itemRect = Rect()
        view.getLocalVisibleRect(itemRect)
        val visibleHeight = itemRect.height().toDouble()
        val height = view.measuredHeight.toDouble()
        return visibleHeight / height
    }

    private fun getVisibleWidthPercentage(view: View): Double {
        val itemRect = Rect()
        view.getLocalVisibleRect(itemRect)
        val visibleWidth = itemRect.width().toDouble()
        val width = view.measuredWidth.toDouble()
        return visibleWidth / width
    }

    private fun addItemToTrackInfoList(trackInfo: TrackInfo) {
        val timeInMs: Long = trackInfoMap[trackInfo.viewTag]?.viewDuration ?: 0L
        trackInfoMap[trackInfo.viewTag] = TrackInfo(
            trackInfo.viewTag,
            trackInfo.itemPosition,
            trackInfo.viewDuration + timeInMs
        )
        if (trackInfoMap.size >= dataLimit) {
            trackAllAndClear()
        }
    }

    private fun trackAllAndClear() {
        if (trackInfoMap.isEmpty()) return
        trackAll?.invoke(trackInfoMap.values.toList())
        trackInfoMap.clear()
    }

    internal fun listUpdated() {
        trackOnGlobalLayout = false
    }

    internal fun trackOnStop() {
        currentListMap.forEach {
            trackItemAndAddToList(it)
        }
        currentListMap.clear()
        trackAllAndClear()
        trackOnGlobalLayout = false
    }

    private fun trackItemAndAddToList(itemMap: Map.Entry<String, TrackData>) {
        val trackInfo = TrackInfo(
            itemMap.key,
            itemMap.value.position,
            System.currentTimeMillis() - itemMap.value.startTime
        )
        if (trackInfo.viewDuration >= minTimeInMs) {
            trackItem?.invoke(trackInfo)
            addItemToTrackInfoList(trackInfo)
        }
    }

}


