package com.rvtimetracker

/**
 * @param viewTag used as key for storing tracking info. Can be used to pass meta data that can be retired at client side
 * @param itemPosition position of item being tracked
 * @param viewDuration Time spent in millisecond on each item being tracked
 */
data class TrackInfo(val viewTag: String, val itemPosition: Int, val viewDuration: Long)