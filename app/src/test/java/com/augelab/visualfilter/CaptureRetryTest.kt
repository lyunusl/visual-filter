package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
class CaptureRetryTest {
 @Test fun successfulFramesHaveNoArtificialDelay(){val r=CaptureRetry();assertEquals(0L,r.remainingMs(100));r.success();assertEquals(0L,r.remainingMs(100))}
 @Test fun rateLimitRetriesAreBounded(){val r=CaptureRetry();assertEquals(25L,r.failed(100,true));assertEquals(50L,r.failed(100,true));repeat(30){r.failed(100,true)};assertEquals(400L,r.remainingMs(100));assertEquals(0L,r.remainingMs(500))}
 @Test fun persistentFailureDoesNotBusySpin(){val r=CaptureRetry();repeat(30){r.failed(100,false)};assertEquals(2000L,r.remainingMs(100));assertEquals(1L,r.remainingMs(2099));assertEquals(0L,r.remainingMs(2100))}
 @Test fun recoveryImmediatelyRemovesBackoff(){val r=CaptureRetry();repeat(8){r.failed(100,false)};r.requested(101);r.success();assertEquals(0L,r.remainingMs(101));r.requested(201);assertEquals(100L,r.failed(201,false))}
 @Test fun learnsActualPlatformLimitInsteadOfRepeatingErrors(){
  val r=CaptureRetry();var now=0L;var accepted=0L;r.requested(now);r.success()
  for(limit in listOf(333L,500L)){
   var lateErrors=0
   repeat(50){i->
    now+=maxOf(100,r.remainingMs(now));r.requested(now)
    if(now-accepted>=limit){r.success();accepted=now}else{r.failed(now,true);if(i>=30)lateErrors++}
   }
   assertEquals(0,lateErrors);assertTrue(r.platformSpacingMs in (limit-2)..(limit+8))
  }
  r.reset();assertEquals(0L,r.remainingMs(now));assertEquals(0L,r.platformSpacingMs)
 }
}
