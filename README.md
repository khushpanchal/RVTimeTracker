[![](https://jitpack.io/v/khushpanchal/RVTimeTracker.svg)](https://jitpack.io/#khushpanchal/RVTimeTracker)

<img src=https://github.com/khushpanchal/RVTimeTracker/blob/master/assets/RVTimeTracker.jpg >

# RVTimeTracker -  RecyclerView Time Tracker

RVTimeTracker (RecyclerView Time Tracker) is an Android library designed to precisely track the time spent by users viewing items in a RecyclerView. Whether you're building an app that needs detailed insights into user interactions or simply want to optimize the user experience, RVTimeTracker has you covered.

## Installation

To integrate RecyclerView Time Tracker library into your Android project, follow these simple steps:

1. Update your settings.gradle file with the following dependency.
   
```Groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' } // this one
    }
}
```

2. Update your module level build.gradle file with the following dependency.
   
```Groovy
dependencies {
        implementation 'com.github.khushpanchal:RVTimeTracker:1.0.1'
}
```

## Usage

Initialization:

Initializing RVTimeTracker is a breeze:

```Kotlin
// Initialize the RecyclerView Time Tracker library in your activity or fragment
RVTimeTracker.init(
    recyclerView = binding.recyclerView, // The RecyclerView to be tracked for item view times.
    minTimeInMs = 1000, // (optional, default = 0): Minimum time in milliseconds a view needs to be visible to be tracked. Value should be greater than 0
    minHeightInRatio = 0.45, // (optional, default = 0.5): Minimum height ratio a view should have to be tracked. Value should be between 0 and 1
    minWidthInRatio = 0.45, // (optional, default = 0.5): Minimum width ratio a view should have to be tracked. Value should be between 0 and 1
    dataLimit = 8, // (optional, default = 10): Number of data after which "trackAll" lambda block will be invoked with the list of tracked data.
    trackItem = { trackInfo -> // Lambda function to be executed when any item moves out of the visible screen. (Contain TrackInfo)
        Log.i("TrackData", trackInfo.toString())
    },
    trackAll = { trackInfoList -> // Lambda function to be executed when the dataLimit is reached or after onStop lifecycle method of the RecyclerView context is called. (Contains List<TrackInfo>)
        for (item in trackInfoList) {
            Log.i("TrackDataAll", item.toString())
        }
    }
)
```

RVTimeTracker provides valuable data in the form of TrackInfo, including view tags(passed by client), item position, and view duration, to gain deep insights into user interactions.
Library will give the following class instance at each callback

```Kotlin
data class TrackInfo(
      val viewTag: String, // used as key for storing tracking info. Can be used to pass meta data that can be retired at client side
      val itemPosition: Int, // position of item being tracked
      val viewDuration: Long // Time spent in millisecond on each item being tracked
)
```

## Important Note

For seamless tracking, it's essential to use a unique view tag for each view inside `onBindViewHolder`. The view tag acts as a key to store tracking information. For example, if you're displaying a list of names in your RecyclerView, ensure that each item has a unique view tag. RVTimeTracker will throw `IllegalArgumentException` if view does not contain view tag.

For example: Recycler view showing list of names, then:

```Kotlin
data class Name(val id: String, val name: String)

val itemList = listOf(
    Name("John", UUID.randomUUID().toString()),
    Name("Jane", UUID.randomUUID().toString()),
    Name("Alice", UUID.randomUUID().toString()),
    // Add more data items...
)
```

Inside `onBindViewHolder`:

```Kotlin
override fun onBindViewHolder(holder: YourViewHolder, position: Int) {
    val item = itemList[position]
    // Set the unique view tag for each item
    holder.itemView.tag = item.id
    // Bind other views...
}
```

## Sample Logs

<img src=https://github.com/khushpanchal/RVTimeTracker/blob/master/assets/RVTimeTracker_Visual.jpg >

Check out these sample logs to see the kind of insights you can gain with RVTimeTracker (RecyclerView Time Tracker):

```
TrackInfo(viewTag=d73bbb42-c43b-4c5c-9b2b-7e9fb7c6f66b, itemPosition=0, viewDuration=30448)
TrackInfo(viewTag=e9c758ff-4eae-423a-afa0-3dd7b18e4e2a, itemPosition=1, viewDuration=54317)
TrackInfo(viewTag=571a4cb8-0b54-4f5a-bfd1-f98444c97049, itemPosition=2, viewDuration=55322)
```

## Blog

Check out the blog: https://medium.com/@khush.panchal123/introducing-rvtimetracker-recyclerview-time-tracker-ad9cae2940f9

## Contribution

Enhance your app's user experience and gain insights into user interactions with RVTimeTracker.
If you have feedback, want to report an issue, or contribute to RVTimeTracker, head over to [GitHub repository](https://github.com/khushpanchal/RecyclerViewTracking/)

