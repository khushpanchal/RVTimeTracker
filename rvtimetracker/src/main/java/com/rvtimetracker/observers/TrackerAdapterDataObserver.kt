package com.rvtimetracker.observers

import androidx.recyclerview.widget.RecyclerView
import com.rvtimetracker.RVTimeTracker

internal class TrackerAdapterDataObserver(private val rVTimeTracker: RVTimeTracker) :
    RecyclerView.AdapterDataObserver() {

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        rVTimeTracker.listUpdated()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        super.onItemRangeRemoved(positionStart, itemCount)
        rVTimeTracker.listUpdated()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount)
        rVTimeTracker.listUpdated()
    }
}