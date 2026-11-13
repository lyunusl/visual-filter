package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class MediaLayoutTest {
 private val window=PixelRect(0,24,1000,2024)
 @Test fun controlsAndTextNeverBecomeMedia(){val x=mediaLayout(window,listOf(MediaCandidate("android.widget.TextView",window),MediaCandidate("android.widget.ImageView",PixelRect(20,40,120,140))),MediaRegion());assertFalse(x.automatic)}
 @Test fun twoFeedImagesRemainSeparate(){val a=PixelRect(0,200,1000,800);val b=PixelRect(0,1000,1000,1600);val x=mediaLayout(window,listOf(MediaCandidate("android.widget.ImageView",a),MediaCandidate("android.widget.ImageView",b)),MediaRegion());assertTrue(x.automatic);assertEquals(listOf(a,b),x.rectangles)}
 @Test fun clipPartlyVisibleMediaToWindow(){val x=mediaLayout(window,listOf(MediaCandidate("android.view.SurfaceView",PixelRect(0,-100,1000,600))),MediaRegion());assertEquals(PixelRect(0,24,1000,600),x.rectangles.single())}
 @Test fun nestedNodesDoNotDuplicateInference(){val a=PixelRect(0,100,1000,1500);val b=PixelRect(0,300,1000,800);val x=mediaLayout(window,listOf(a,a,b).map{MediaCandidate("android.widget.ImageView",it)},MediaRegion());assertEquals(listOf(b),x.rectangles)}
 @Test fun unsupportedLayoutNeverUsesManualFallback(){val w=PixelRect(100,200,1100,2200);val x=mediaLayout(w,emptyList(),MediaRegion(0f,.25f,1f,.65f));assertFalse(x.automatic);assertTrue(x.rectangles.isEmpty())}
 @Test fun instagramRejectsGenericFullScreenImage(){val x=mediaLayout(window,listOf(MediaCandidate("android.widget.ImageView",window)),MediaRegion(),allowGeneric=false);assertTrue(x.rectangles.isEmpty())}
 @Test fun tightPhotoWinsOverOuterVideoContainer(){val image=PixelRect(0,300,1000,900);val x=mediaLayout(window,listOf(MediaCandidate("android.widget.FrameLayout",window,"com.instagram.android:id/video_container"),MediaCandidate("android.widget.FrameLayout",image,"com.instagram.android:id/carousel_image")),MediaRegion(),false);assertEquals(listOf(image),x.rectangles)}
 @Test fun scrollUpdatesMediaGeometry(){val a=mediaLayout(window,listOf(MediaCandidate("android.widget.ImageView",PixelRect(0,600,1000,1400))),MediaRegion());val b=mediaLayout(window,listOf(MediaCandidate("android.widget.ImageView",PixelRect(0,200,1000,1000))),MediaRegion());assertNotEquals(a.rectangles,b.rectangles)}
 @Test fun observedInstagramCarouselFrameExcludesCaptionAndFeedContainer(){
  val image=PixelRect(0,200,1000,1400)
  val x=mediaLayout(window,listOf(MediaCandidate("android.widget.FrameLayout",window,"com.instagram.android:id/swipeable_tab_view_pager"),MediaCandidate("android.widget.FrameLayout",image,"com.instagram.android:id/carousel_image"),MediaCandidate("android.view.ViewGroup",PixelRect(0,1400,1000,1800))),MediaRegion())
  assertTrue(x.automatic);assertEquals(listOf(image),x.rectangles)
 }
 @Test fun observedCarouselParentAndImageProduceOneClippedRegion(){
  val image=PixelRect(0,-100,1000,900)
  val x=mediaLayout(window,listOf(MediaCandidate("android.widget.FrameLayout",image,"com.instagram.android:id/carousel_image_media_group"),MediaCandidate("android.widget.FrameLayout",image,"com.instagram.android:id/carousel_image")),MediaRegion())
  assertEquals(listOf(PixelRect(0,24,1000,900)),x.rectangles)
 }
 @Test fun foreignResourceOrGenericContainerDoesNotBecomeAnImage(){
  val x=mediaLayout(window,listOf(MediaCandidate("android.widget.FrameLayout",window,"other.app:id/carousel_image"),MediaCandidate("android.view.ViewGroup",window)),MediaRegion())
  assertFalse(x.automatic)
 }
 @Test fun carouselScrollRetainsSeparateMediaAndRejectsControls(){
  val a=PixelRect(0,-100,1000,700);val b=PixelRect(0,1000,1000,1700)
  val x=mediaLayout(window,listOf(MediaCandidate("android.widget.FrameLayout",a,"com.instagram.android:id/carousel_image"),MediaCandidate("android.widget.FrameLayout",b,"com.instagram.android:id/carousel_image"),MediaCandidate("android.widget.Button",window)),MediaRegion())
  assertEquals(listOf(PixelRect(0,24,1000,700),b),x.rectangles)
 }
 @Test fun observedSinglePhotoFrameDoesNotIncludeCaption(){
  val image=PixelRect(0,200,1000,1200)
  val x=mediaLayout(window,listOf(MediaCandidate("android.widget.FrameLayout",image,"com.instagram.android:id/row_feed_photo_imageview"),MediaCandidate("android.view.ViewGroup",PixelRect(0,1200,1000,1700))),MediaRegion())
  assertTrue(x.automatic);assertEquals(listOf(image),x.rectangles)
 }
 @Test fun observedVideoGroupsStaySeparateAcrossCaptionGap(){
  val a=PixelRect(0,-100,1000,600);val b=PixelRect(0,950,1000,1900)
  val x=mediaLayout(window,listOf(MediaCandidate("android.view.ViewGroup",a,"com.instagram.android:id/video_container"),MediaCandidate("android.widget.FrameLayout",a,"com.instagram.android:id/media_group"),MediaCandidate("android.widget.FrameLayout",b,"com.instagram.android:id/media_group"),MediaCandidate("android.view.ViewGroup",PixelRect(0,600,1000,950))),MediaRegion())
  assertTrue(x.automatic);assertEquals(listOf(PixelRect(0,24,1000,600),b),x.rectangles)
 }
}
