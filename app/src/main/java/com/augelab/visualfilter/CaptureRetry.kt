package com.augelab.visualfilter

/** Starts uncapped; only Android rate-limit responses establish a learned minimum interval. */
class CaptureRetry {
 private var failures=0
 private var nextMs=0L
 private var requestedMs=0L
 private var lastSuccessMs:Long?=null
 private var lowerMs=0L
 private var upperMs:Long?=null
 var platformSpacingMs=0L;private set
 fun reset(){failures=0;nextMs=0;requestedMs=0;lastSuccessMs=null;lowerMs=0;upperMs=null;platformSpacingMs=0}
 fun requested(now:Long){requestedMs=now}
 fun remainingMs(now:Long)=(maxOf(nextMs,lastSuccessMs?.plus(platformSpacingMs+if(platformSpacingMs>0)2 else 0)?:0)-now).coerceAtLeast(0)
 fun success(){
  val previous=lastSuccessMs
  if(platformSpacingMs>0 && previous!=null){
   val gap=requestedMs-previous
   upperMs=minOf(upperMs?:gap,gap)
   platformSpacingMs=if(upperMs!!-lowerMs<=1)upperMs!! else (lowerMs+upperMs!!+1)/2
  }
  lastSuccessMs=requestedMs;failures=0;nextMs=0
 }
 fun failed(now:Long,rateLimited:Boolean):Long {
  val previous=lastSuccessMs
  if(rateLimited && previous!=null){
   lowerMs=maxOf(lowerMs,requestedMs-previous)
   if(upperMs!=null && lowerMs>=upperMs!!)upperMs=null // Device/OS limit changed.
   platformSpacingMs=upperMs?.let{(lowerMs+it+1)/2}?:maxOf(25,lowerMs+maxOf(25,lowerMs/2))
   nextMs=previous+platformSpacingMs
   return remainingMs(now)
  }
  val step=failures.coerceAtMost(6);failures=(failures+1).coerceAtMost(7)
  val wait=if(rateLimited)(25L shl step).coerceAtMost(400)else (100L shl step).coerceAtMost(2000)
  nextMs=now+wait;return wait
 }
}
