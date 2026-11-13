package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class ReelsMediaTest {
 private val screen=PixelRect(0,0,1080,2400)
 @Test fun observedReelsFrameLayoutIsMedia(){
  val video=PixelRect(0,128,1080,2179)
  val node=MediaCandidate("android.widget.FrameLayout",video,"com.instagram.android:id/clips_video_container","reel-1")
  val result=mediaLayout(screen,listOf(node),MediaRegion(),allowGeneric=false)
  assertEquals(listOf(video),result.rectangles);assertEquals("reel-1",result.tracks.single().identity)
 }
 @Test fun reelsWrappersAndControlsAreNotMedia(){
  val resources=listOf("clips_viewer_view_pager","clips_media_component","clips_single_media_component","clips_item_overlay_component","clips_bottom_legibility_gradient_component")
  val nodes=resources.map{MediaCandidate("android.view.ViewGroup",screen,"com.instagram.android:id/$it")}
  assertTrue(mediaLayout(screen,nodes,MediaRegion(),allowGeneric=false).rectangles.isEmpty())
  val video=PixelRect(0,128,1080,2179);val button=PixelRect(943,1324,1059,1440)
  val pieces=blockedMedia(screen,listOf(video),listOf(ReviewState.BLOCK))
  assertEquals(listOf(video),pieces);assertEquals(button,pieces.single().clipped(button))
 }
}
