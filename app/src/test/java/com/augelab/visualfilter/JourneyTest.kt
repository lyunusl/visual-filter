package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test

class JourneyTest {
 private val a=TrackedMedia("video-a",PixelRect(0,100,1000,1100))
 private val b=TrackedMedia("video-b",PixelRect(0,1200,1000,2000))
 private val moment=JourneyMoment(7,1234)
 private fun counted()=SceneReviewGate().apply{updateTrackedWindow(1,"a",listOf(a));accept(generation,0,true,.9f);assertTrue(markCounted(generation,0,moment))}
 @Test fun repeatedSafeAndBlockFramesDoNotRequestAnotherCount(){val g=counted();repeat(100){g.accept(g.generation,0,it%2==0,.5f)};assertEquals(moment,g.moments[0]);assertFalse(g.markCounted(g.generation,0,JourneyMoment(8,9999)))}
 @Test fun overlappingScrollKeepsCountAndAnimationOrigin(){val g=counted();g.scrolled(true);g.updateTrackedWindow(1,"move",listOf(a.copy(bounds=PixelRect(0,0,1000,1000))));assertEquals(moment,g.moments[0])}
 @Test fun confirmedPagerReplacementCanCountRecycledNodeAgain(){val g=counted();g.scrolled(false);assertNull(g.moments[0]);g.accept(g.generation,0,true);assertTrue(g.markCounted(g.generation,0,JourneyMoment(8,2222)))}
 @Test fun staleResultsCannotCount(){val g=counted();val old=g.generation;g.scrolled(false);g.accept(g.generation,0,true);assertFalse(g.markCounted(old,0,moment));assertNull(g.moments[0])}
 @Test fun safePendingAndMissingRegionsCannotCount(){val g=SceneReviewGate();g.updateWindow(1,"x",2);g.accept(g.generation,0,false);assertFalse(g.markCounted(g.generation,0,moment));assertFalse(g.markCounted(g.generation,1,moment));assertFalse(g.markCounted(g.generation,3,moment))}
 @Test fun simultaneousMediaKeepIndependentCounters(){val g=SceneReviewGate();g.updateTrackedWindow(1,"both",listOf(a,b));g.accept(g.generation,0,true);g.markCounted(g.generation,0,moment);assertNull(g.moments[1]);g.accept(g.generation,1,true);val second=JourneyMoment(8,2222);g.markCounted(g.generation,1,second);assertEquals(listOf(moment,second),g.moments)}
 @Test fun reorderingDoesNotTransferProgressToDifferentMedia(){val g=SceneReviewGate();g.updateTrackedWindow(1,"both",listOf(a,b));g.accept(g.generation,0,true);g.markCounted(g.generation,0,moment);g.updateTrackedWindow(1,"reorder",listOf(b,a));assertEquals(listOf(null,moment),g.moments)}
 @Test fun newIdentityDoesNotInheritProgress(){val g=counted();g.updateTrackedWindow(1,"new",listOf(a.copy(identity="new")));assertNull(g.moments[0])}
 @Test fun leavingRemovesOnlyInMemorySceneMoment(){val g=counted();g.leave();assertTrue(g.moments.isEmpty());assertFalse(g.markCounted(g.generation,0,moment))}
 @Test fun levelCompletesBeforeTheNextLevelStarts(){for((n,level,opened) in listOf(Triple(0L,1L,0),Triple(1L,1L,1),Triple(9L,1L,9),Triple(10L,1L,10),Triple(11L,2L,1),Triple(20L,2L,10))){val x=JourneyMoment(n,0);assertEquals(level,x.level);assertEquals(opened,x.released);assertEquals(10-opened,x.remaining)}}
 @Test fun counterNeverOverflowsOrGoesNegative(){assertEquals(1L,nextBlockTotal(-10));assertEquals(Long.MAX_VALUE,nextBlockTotal(Long.MAX_VALUE));val x=JourneyMoment(Long.MAX_VALUE,0);assertTrue(x.level>0);assertTrue(x.released in 1..10)}
 @Test fun sixConsecutiveEncountersCoverAllThemes(){assertEquals(JourneyTheme.entries.toSet(),(1L..6L).map{JourneyMoment(it,0).theme}.toSet());assertEquals(JourneyTheme.DAWN,JourneyMoment(7,0).theme)}
 @Test fun messagesAreDistinctAndCycleAfterFullLibrary(){assertEquals(36,JourneyMessages.all.size);assertEquals(36,JourneyMessages.all.toSet().size);assertTrue(JourneyMessages.all.all{it.title.isNotBlank() && it.body.isNotBlank()});assertEquals(JourneyMoment(1,0).message,JourneyMoment(37,0).message)}
}
