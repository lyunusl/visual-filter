package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class TrackedMaskTest {
 private val a=PixelRect(0,400,1000,1400);private val moved=PixelRect(0,200,1000,1200)
 private fun gate()=SceneReviewGate().apply{updateTrackedWindow(1,"a",listOf(TrackedMedia("photo1",a)));accept(generation,0,true)}
 @Test fun sameBlockedImageMovesWithoutUncovering(){val g=gate();val old=g.generation;g.updateTrackedWindow(1,"moved",listOf(TrackedMedia("photo1",moved)));assertEquals(ReviewState.BLOCK,g.states[0]);assertTrue(g.retainedBlockDuringMove);assertFalse(g.accept(old,0,false))}
 @Test fun newPostNeverInheritsBlock(){val g=gate();g.updateTrackedWindow(1,"new",listOf(TrackedMedia("photo2",moved)));assertEquals(ReviewState.CHECKING,g.states[0])}
 @Test fun unrelatedLayoutChangeDoesNotUncoverSameTrackedMedia(){val g=gate();g.updateTrackedWindow(1,"new",listOf(TrackedMedia("photo1",a)));assertEquals(ReviewState.BLOCK,g.states[0])}
 @Test fun noIdentityDoesNotCarryBlock(){val g=gate();g.updateTrackedWindow(1,"new",listOf(TrackedMedia(null,moved)));assertEquals(ReviewState.CHECKING,g.states[0])}
 @Test fun nonoverlappingJumpDoesNotCarryBlock(){val g=gate();g.updateTrackedWindow(1,"new",listOf(TrackedMedia("photo1",PixelRect(0,1500,1000,2000))));assertEquals(ReviewState.CHECKING,g.states[0])}
 @Test fun windowSwitchNeverCarriesBlock(){val g=gate();g.updateTrackedWindow(2,"moved",listOf(TrackedMedia("photo1",moved)));assertEquals(ReviewState.CHECKING,g.states[0])}
 @Test fun firstSafeOnUnreviewedImageIsImmediate(){val g=SceneReviewGate();g.updateWindow(1,"a",1);g.accept(g.generation,0,false);assertEquals(ReviewState.SAFE,g.states[0])}
 @Test fun confirmedPageReplacementClearsEvenReusedNode(){val g=gate();val old=g.generation;g.scrolled(false);assertEquals(ReviewState.CHECKING,g.states[0]);assertFalse(g.accept(old,0,true));g.accept(g.generation,0,false);assertEquals(ReviewState.SAFE,g.states[0])}
 @Test fun anotherMediaEnteringDoesNotUncoverStationaryBlockedVideo(){val g=gate();g.updateTrackedWindow(1,"two",listOf(TrackedMedia("photo1",a),TrackedMedia("photo2",PixelRect(0,1450,1000,2000))));assertEquals(listOf(ReviewState.BLOCK,ReviewState.CHECKING),g.states);repeat(20){g.accept(g.generation,0,false)};g.accept(g.generation,1,false);assertEquals(listOf(ReviewState.BLOCK,ReviewState.SAFE),g.states)}
 @Test fun removedMediaDoesNotLeaveMaskOnRemainingSafeMedia(){val g=gate();g.updateTrackedWindow(1,"next",listOf(TrackedMedia("photo2",a)));g.accept(g.generation,0,false);assertEquals(ReviewState.SAFE,g.states[0])}
 @Test fun intermittentSafeVideoFrameDoesNotFlicker(){val g=gate();g.accept(g.generation,0,false);assertEquals(ReviewState.BLOCK,g.states[0]);g.accept(g.generation,0,true);g.accept(g.generation,0,false);assertEquals(ReviewState.BLOCK,g.states[0]);repeat(100){g.accept(g.generation,0,false)};assertEquals(ReviewState.BLOCK,g.states[0])}
 @Test fun scrollInvalidatesInferenceWhileRetainingTrackedBlock(){val g=gate();val old=g.generation;g.scrolled(true);assertEquals(ReviewState.BLOCK,g.states[0]);assertFalse(g.accept(old,0,false))}
 @Test fun departingSliverRetainsBlockedImageUntilItLeaves(){val g=gate();val sliver=PixelRect(0,0,1000,100);g.updateTrackedWindow(1,"edge",listOf(TrackedMedia("photo1",PixelRect(0,0,1000,600))));g.updateTrackedWindow(1,"sliver",listOf(TrackedMedia("photo1",sliver)));assertEquals(ReviewState.BLOCK,g.states[0]);assertFalse(isReviewableMedia(PixelRect(0,0,1000,2000),sliver))}
 @Test fun tinyExposedMediaIsTrackableWithoutReclassifyingCrop(){val w=PixelRect(0,0,1000,2000);val r=PixelRect(0,0,1000,100);val x=mediaLayout(w,listOf(MediaCandidate("android.widget.FrameLayout",r,"com.instagram.android:id/carousel_image","id")),MediaRegion(),false);assertEquals(listOf(r),x.rectangles);assertFalse(isReviewableMedia(w,r))}
}
