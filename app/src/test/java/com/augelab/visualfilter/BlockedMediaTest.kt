package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class BlockedMediaTest {
 private val window=PixelRect(0,0,1000,2000)
 private val a=PixelRect(0,200,1000,700);private val b=PixelRect(0,1000,1000,1500)
 @Test fun pendingAndMissingDecisionsStayVisible(){assertTrue(blockedMedia(window,listOf(a,b),listOf(ReviewState.CHECKING)).isEmpty())}
 @Test fun onlyBlockCreatesImageWindows(){assertEquals(listOf(b),blockedMedia(window,listOf(a,b),listOf(ReviewState.SAFE,ReviewState.BLOCK)))}
 @Test fun blockedImagesNeverCoverCaptionGap(){assertEquals(listOf(a,b),blockedMedia(window,listOf(a,b),listOf(ReviewState.BLOCK,ReviewState.BLOCK)))}
 @Test fun scrollRemovesOldBlockAndRejectsLateResult(){val gate=SceneReviewGate();gate.updateWindow(1,"images",2);val old=gate.generation;assertTrue(gate.accept(old,0,true));gate.scrolled();assertTrue(blockedMedia(window,listOf(a,b),gate.states).isEmpty());assertFalse(gate.accept(old,1,true))}
 @Test fun entireMediaIsOneSurfaceIncludingOverlaidControls(){val button=PixelRect(100,500,900,650);val pieces=blockedMedia(window,listOf(a),listOf(ReviewState.BLOCK));assertEquals(listOf(a),pieces);assertEquals(button,pieces.single().clipped(button));assertEquals(a.width*a.height,pieces.sumOf{it.width*it.height})}
 @Test fun departingMediaClipsToWindowWithoutWidening(){assertEquals(listOf(PixelRect(0,0,1000,100)),blockedMedia(window,listOf(PixelRect(0,-400,1000,100)),listOf(ReviewState.BLOCK)))}
 @Test fun unsupportedLayoutCreatesNoOverlay(){assertTrue(blockedMedia(window,emptyList(),listOf(ReviewState.BLOCK)).isEmpty())}
}
