package com.augelab.visualfilter

/** New media first. Latched blocks need no more inference unless live debug scores are requested. */
fun reviewOrder(window:PixelRect,rectangles:List<PixelRect>,states:List<ReviewState>,debugScores:Boolean):List<Int> =
 rectangles.indices.filter{isReviewableMedia(window,rectangles[it]) && (debugScores || states.getOrNull(it)!=ReviewState.BLOCK)}
  .sortedBy{when(states.getOrNull(it)){ReviewState.SAFE->1;ReviewState.BLOCK->2;else->0}}
