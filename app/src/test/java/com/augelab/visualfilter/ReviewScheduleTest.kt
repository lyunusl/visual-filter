package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class ReviewScheduleTest {
 private val window=PixelRect(0,0,1000,2000)
 private val rects=listOf(PixelRect(0,0,1000,500),PixelRect(0,600,1000,1100),PixelRect(0,1200,1000,1800))
 @Test fun newlyArrivedImagePrecedesSafeAndLatchedImages(){assertEquals(listOf(2,0,1),reviewOrder(window,rects,listOf(ReviewState.SAFE,ReviewState.BLOCK,ReviewState.CHECKING),true))}
 @Test fun normalModeDoesNotReclassifyPermanentBlocks(){assertEquals(listOf(2,0),reviewOrder(window,rects,listOf(ReviewState.SAFE,ReviewState.BLOCK,ReviewState.CHECKING),false))}
 @Test fun allBlockedNormalModeNeedsNoScreenshot(){assertTrue(reviewOrder(window,rects,List(3){ReviewState.BLOCK},false).isEmpty())}
 @Test fun debugKeepsLatestScoresForBlockedVideo(){assertEquals(listOf(0,1,2),reviewOrder(window,rects,List(3){ReviewState.BLOCK},true))}
 @Test fun smallDepartingSliceIsNotClassified(){assertTrue(reviewOrder(window,listOf(PixelRect(0,0,1000,50)),listOf(ReviewState.CHECKING),true).isEmpty())}
 @Test fun changedMediaResumesReview(){val gate=SceneReviewGate();gate.updateWindow(1,"first",1);gate.accept(gate.generation,0,true);assertTrue(reviewOrder(window,rects.take(1),gate.states,false).isEmpty());gate.updateWindow(1,"second",1);assertEquals(listOf(0),reviewOrder(window,rects.take(1),gate.states,false))}
}
