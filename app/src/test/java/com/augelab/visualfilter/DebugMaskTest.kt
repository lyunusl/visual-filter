package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class DebugMaskTest {
 @Test fun firstBlockScoreStaysDistinctFromLatestSafeScore(){val g=SceneReviewGate();g.updateWindow(1,"a",1);g.accept(g.generation,0,true,.9f);repeat(10){g.accept(g.generation,0,false,.1f)};assertEquals(listOf(.9f),g.blockScores);assertEquals(listOf(.1f),g.scores);g.scrolled(true);assertEquals(listOf(.9f),g.blockScores);g.scrolled(false);assertEquals(listOf(null),g.blockScores)}
 @Test fun invalidOrStaleScoresCannotBecomeTriggerScore(){val g=SceneReviewGate();g.updateWindow(1,"a",1);val old=g.generation;g.scrolled();assertFalse(g.accept(old,0,true,.9f));g.accept(g.generation,0,true,Float.NaN);assertEquals(listOf(null),g.blockScores);assertEquals(listOf(null),g.scores)}
 @Test fun scoreMovesWithBlockedImage(){val g=SceneReviewGate();g.updateTrackedWindow(1,"a",listOf(TrackedMedia("x",PixelRect(0,0,100,100))));g.accept(g.generation,0,true,.9f);g.updateTrackedWindow(1,"b",listOf(TrackedMedia("x",PixelRect(0,10,100,110))));assertEquals(listOf(.9f),g.scores)}
 @Test fun newIdentityNeverInheritsScore(){val g=SceneReviewGate();g.updateTrackedWindow(1,"a",listOf(TrackedMedia("x",PixelRect(0,0,100,100))));val old=g.generation;g.accept(old,0,true,.9f);g.updateTrackedWindow(1,"b",listOf(TrackedMedia("y",PixelRect(0,0,100,100))));assertFalse(g.accept(old,0,true,.8f));assertEquals(listOf(null),g.scores)}
 @Test fun debugUsesLatestFrameScoreWhileBlockIsLatched(){val g=SceneReviewGate();g.updateWindow(1,"a",1);g.accept(g.generation,0,true,.9f);g.accept(g.generation,0,false,.1f);assertEquals(ReviewState.BLOCK,g.states[0]);assertEquals(.1f,g.scores[0]);g.scrolled();assertEquals(listOf(null),g.scores)}
 @Test fun labelOnlyInLargestPieceOfOwnImage(){val media=listOf(PixelRect(0,0,100,100),PixelRect(0,110,100,200));val pieces=listOf(PixelRect(0,0,100,10),PixelRect(0,30,100,100),media[1]);assertEquals(listOf("","first","second"),maskLabels(pieces,media,listOf("first","second")));assertEquals(listOf("","",""),maskLabels(pieces,media,emptyList()))}
}
