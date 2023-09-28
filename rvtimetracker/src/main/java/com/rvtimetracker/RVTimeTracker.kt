package com.rvtimetracker

import android.graphics.Rect
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rvtimetracker.observers.TrackerAdapterDataObserver
import com.rvtimetracker.observers.TrackerObserver

class RVTimeTracker private constructor(
    private val recyclerView: RecyclerView,
    private val minTimeInMs: Long,
    private val minHeightInRatio: Double,
    private val dataLimit: Int,
    private val trackItem: ((TrackInfo) -> Unit)?,
    private val trackAll: ((List<TrackInfo>) -> Unit)?
) {
    companion object {

        fun init(
            recyclerView: RecyclerView,
            minTimeInMs: Long = 0L,
            minHeightInRatio: Double = 0.5,
            dataLimit: Int = 10,
            trackItem: ((TrackInfo) -> Unit)? = null,
            trackAll: ((List<TrackInfo>) -> Unit)? = null
        ) {
            if (dataLimit > 50 || minTimeInMs < 0 || minHeightInRatio < 0 || minHeightInRatio > 1) {
                throw IllegalArgumentException("dataLimit/minTimeInMs/minHeightInRatio value should be within limit")
            }
            RVTimeTracker(
                recyclerView,
                minTimeInMs,
                minHeightInRatio,
                dataLimit,
                trackItem,
                trackAll
            )
        }
    }

    private val currentListMap = mutableMapOf<String, TrackData>()
    private val newListMap = mutableMapOf<String, TrackData>()
    private val trackInfoMap = mutableMapOf<String, TrackInfo>()
    private var trackOnGlobalLayout = false

    init {

        registerObservers()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                addNewListAndTrack()
            }
        })

        recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!trackOnGlobalLayout) {
                trackOnGlobalLayout = true
                if(!recyclerView.isAnimating) {
                    addNewListAndTrack()
                } else {
                    recyclerView.itemAnimator?.isRunning {
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
            if (itemView != null && getVisibleHeightPercentage(itemView) >= minHeightInRatio) {
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


