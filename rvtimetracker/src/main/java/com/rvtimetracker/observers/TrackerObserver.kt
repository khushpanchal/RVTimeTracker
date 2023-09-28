package com.rvtimetracker.observers

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.rvtimetracker.RVTimeTracker

internal class TrackerObserver(private val rVTimeTracker: RVTimeTracker) :
    DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        rVTimeTracker.trackOnStop()
    }
}