package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class SceneReviewGateTest {
 private fun gate()=SceneReviewGate().apply{updateWindow(4,"region-a",1)}
 @Test fun firstHighBlocksImmediately(){val g=gate();assertTrue(g.accept(g.generation,0,true));assertEquals(ReviewState.BLOCK,g.states[0])}
 @Test fun blockedSceneRemainsBlockedAcrossSafeFrames(){val g=gate();g.accept(g.generation,0,true);g.accept(g.generation,0,false);assertEquals(ReviewState.BLOCK,g.states[0]);repeat(100){g.accept(g.generation,0,false)};assertEquals(ReviewState.BLOCK,g.states[0])}
 @Test fun scrollConcealsPreviouslySafeScene(){val g=gate();g.accept(g.generation,0,false);g.scrolled();assertEquals(ReviewState.CHECKING,g.states[0])}
 @Test fun staleSafeCannotUncoverNextPost(){val g=gate();val old=g.generation;g.scrolled();assertFalse(g.accept(old,0,false));assertEquals(ReviewState.CHECKING,g.states[0])}
 @Test fun staleBlockCannotMaskNextPost(){val g=gate();val old=g.generation;g.scrolled();g.accept(g.generation,0,false);assertFalse(g.accept(old,0,true));assertEquals(ReviewState.SAFE,g.states[0])}
 @Test fun unchangedLayoutDoesNotInvalidateOnOrdinaryContentNotifications(){val g=gate();g.accept(g.generation,0,false);val epoch=g.generation;assertFalse(g.updateWindow(4,"region-a",1));assertEquals(epoch,g.generation);assertEquals(ReviewState.SAFE,g.states[0])}
 @Test fun mediaGeometryChangeInvalidates(){val g=gate();val old=g.generation;assertTrue(g.updateWindow(4,"region-b",1));assertFalse(g.accept(old,0,false));assertEquals(ReviewState.CHECKING,g.states[0])}
 @Test fun switchedWindowRejectsOldResult(){val g=gate();val old=g.generation;g.updateWindow(5,"region-a",1);assertFalse(g.accept(old,0,false))}
 @Test fun leavingInstagramHidesAllMasks(){val g=gate();val old=g.generation;g.leave();assertTrue(g.states.isEmpty());assertNull(g.windowId);assertFalse(g.accept(old,0,true))}
 @Test fun independentMediaRegionsDoNotUncoverEachOther(){val g=SceneReviewGate();g.updateWindow(4,"two",2);g.accept(g.generation,0,false);assertEquals(listOf(ReviewState.SAFE,ReviewState.CHECKING),g.states);g.accept(g.generation,1,true);assertEquals(listOf(ReviewState.SAFE,ReviewState.BLOCK),g.states)}
 @Test fun invalidIndexCannotApply(){val g=gate();assertFalse(g.accept(g.generation,1,false));assertEquals(ReviewState.CHECKING,g.states[0])}
 @Test fun noWindowCannotApply(){assertFalse(SceneReviewGate().accept(0,0,false))}
 @Test fun missingResultNeverClearsChecking(){val g=gate();g.scrolled();assertEquals(ReviewState.CHECKING,g.states[0])}
}
