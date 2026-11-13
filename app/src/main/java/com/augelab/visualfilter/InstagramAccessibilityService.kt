package com.augelab.visualfilter
import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*
import java.util.ArrayDeque

object AccessibilityFilterControl {
 var service:InstagramAccessibilityService?=null
 fun start(context:android.content.Context){
  val connected=service
  if(connected!=null){connected.startFiltering();return}
  val expected=android.content.ComponentName(context,InstagramAccessibilityService::class.java)
  val enabled=android.provider.Settings.Secure.getString(context.contentResolver,android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
   ?.split(':')?.any{android.content.ComponentName.unflattenFromString(it)==expected}==true
  SessionState.set(UiStatus(message=accessibilityConnectionMessage(enabled)))
 }
 suspend fun stop(context:android.content.Context,pin:String):Boolean {
  val repo=FilterRepository(context)
  val authorized=withContext(Dispatchers.Default){repo.verifyStopPin(pin)}
  if(!authorized)return false
  if(!repo.setFilterRequested(false))return false
  service?.stopAfterAuthorization()
  SessionState.set(UiStatus(message="Filtre PIN ile durduruldu."))
  return true
 }
}

/** API34 window capture excludes accessibility overlays. Screenshots live only in memory.
 * No gesture injection, touch exploration, content text, grants, other-app captures or disk images.
 */
class InstagramAccessibilityService:AccessibilityService(){
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
 private val gate=SceneReviewGate()
 private var mask:AccessibilityMask?=null
 private var model:OnnxResearchClassifier?=null
 private var running=false;private var ready=false;private var busy=false
 private var lifecycle=0L;private var completed=0L;private var stale=0L
 private val captureRetry=CaptureRetry()
 private var capturePump:Job?=null
 private var captureRequests=0L
 private var lastScrollUptimeMs:Long?=null
 private var lastScrollSignature:String?=null
 private var lastSingleItem:String?=null
 private var lastEventDiagnosticMs=0L;private var lastLayoutDiagnostic=""
 private var repo:FilterRepository?=null
 private data class Target(val id:Int,val bounds:PixelRect,val layout:MediaLayout)
 private var target:Target?=null
 override fun onServiceConnected(){
  AccessibilityFilterControl.service=this
  val saved=FilterRepository(this)
  if(saved.filterRequested() && saved.hasStopPin())startFiltering()
  else SessionState.set(UiStatus(message="Erişilebilirlik bağlantısı hazır. Filtreyi uygulamadan başlatın."))
 }
 // Android asks to interrupt accessibility feedback here, not to disable this filter.
 override fun onInterrupt(){}
 override fun onUnbind(intent:android.content.Intent?):Boolean{stopFiltering();if(AccessibilityFilterControl.service===this)AccessibilityFilterControl.service=null;return super.onUnbind(intent)}
 override fun onDestroy(){stopFiltering();if(AccessibilityFilterControl.service===this)AccessibilityFilterControl.service=null;scope.cancel();super.onDestroy()}
 fun startFiltering(){
  if(running)return
  val saved=FilterRepository(this)
  if(!saved.hasStopPin()){SessionState.set(UiStatus(message="Başlatmadan önce durdurma PIN'i belirleyin."));return}
  if(!saved.setFilterRequested(true)){SessionState.set(UiStatus(message="Başlatma durumu kaydedilemedi."));return}
  if(Build.VERSION.SDK_INT<34){SessionState.set(UiStatus(message="Bu akış Android14 veya üzerini gerektirir."));return}
  running=true;ready=false;busy=false;lifecycle++;val session=lifecycle
  repo=FilterRepository(this);mask=AccessibilityMask(this);captureRetry.reset();completed=0;stale=0;captureRequests=0
  SessionState.set(UiStatus(true,"Modeller hazırlanıyor; inceleme sırasında görsel açık kalır."))
  scope.launch{
   var created:OnnxResearchClassifier?=null
   try{
    val loaded=withContext(Dispatchers.Default){OnnxResearchClassifier(this@InstagramAccessibilityService,repo!!.threshold()).also{created=it;it.warmUp()}}
    if(running && lifecycle==session){model=loaded;ready=true;SessionState.set(UiStatus(true,"Hazır. Instagram’ı açın."));requestNextCapture()}
   }catch(e:CancellationException){throw e}catch(e:Exception){recoverFiltering("Model hazırlanamadı: ${e.javaClass.simpleName}")}
   finally{if(model!==created)created?.close()}
  }
  scope.launch{while(isActive && running && lifecycle==session){try{tick()}catch(e:Exception){if(e is CancellationException)throw e;recoverFiltering("İnceleme kesildi: ${e.javaClass.simpleName}")};delay(100)}}
 }
 private fun recoverFiltering(reason:String){
  stopFiltering()
  SessionState.set(UiStatus(true,"$reason. Şu an koruma yok; 5 saniye sonra yeniden denenecek."))
  scope.launch{delay(5000);if(FilterRepository(this@InstagramAccessibilityService).filterRequested())startFiltering()}
 }
 internal fun stopAfterAuthorization(){check(!FilterRepository(this).filterRequested());stopFiltering()}
 private fun stopFiltering(){
  running=false;ready=false;lifecycle++;capturePump?.cancel();capturePump=null;gate.leave();lastScrollSignature=null;target=null;mask?.close();mask=null
  val old=model;model=null
  val jobs=scope.coroutineContext[Job]?.children?.toList().orEmpty();scope.coroutineContext.cancelChildren()
  val diagnostic=repo?.developer()==true
  fun releaseModel(){old?.close();if(diagnostic)Log.i("VisualFilterScroll","event=model_closed_after_stop had_model=${old!=null}")}
  if(jobs.isEmpty())releaseModel()else{val remaining=java.util.concurrent.atomic.AtomicInteger(jobs.size);jobs.forEach{it.invokeOnCompletion{if(remaining.decrementAndGet()==0)releaseModel()}}}
  if(diagnostic)Log.i("VisualFilterScroll","event=filter_stopped pending_jobs=${jobs.size} overlay_attached=${mask?.attached==true} running=$running")
  SessionState.set(UiStatus(message="Filtre durduruldu."))
 }
 override fun onAccessibilityEvent(event:AccessibilityEvent?){
  if(!running || event==null)return
  if(repo?.developer()==true && isTargetPackage(event.packageName?.toString())){
   val now=SystemClock.uptimeMillis()
   if(event.eventType==AccessibilityEvent.TYPE_VIEW_SCROLLED || now-lastEventDiagnosticMs>=500){
    lastEventDiagnosticMs=now
    Log.i("VisualFilterScroll","event=accessibility_metadata event_type=${event.eventType} content_change_types=${event.contentChangeTypes} window_id=${event.windowId} event_uptime_ms=${event.eventTime} from_index=${event.fromIndex} to_index=${event.toIndex} item_count=${event.itemCount} source_class=${event.className?.toString()?.replace(Regex("[^A-Za-z0-9_.$]"),"_")?.take(120)} scroll_y=${event.scrollY} scroll_delta_y=${event.scrollDeltaY}")
   }
  }
  if(isTargetPackage(event.packageName?.toString()) && event.eventType==AccessibilityEvent.TYPE_VIEW_SCROLLED){
   val signature="${event.windowId}:${event.className}:${event.fromIndex}:${event.toIndex}:${event.scrollX}:${event.scrollY}"
   val unchangedZero=event.scrollDeltaX==0 && event.scrollDeltaY==0 && lastScrollSignature==signature
   lastScrollSignature=signature
   val singleItem=if(event.fromIndex>=0 && event.fromIndex==event.toIndex)"${event.windowId}:${event.className}:${event.fromIndex}"else null
   val switchedSingleItem=singleItem!=null && singleItem!=lastSingleItem
   lastSingleItem=singleItem
   syncTarget()
   // Instagram can report scroll before its cached node bounds move. Keep the current
   // image's BLOCK until refreshed geometry/identity removes or moves it. A changed
   // single-item range (Reels-style paging) starts a new visible review instead.
   if(!unchangedZero){gate.scrolled(preserveBlocks=!switchedSingleItem);lastScrollUptimeMs=event.eventTime;render();requestNextCapture()}
  }else if(event.eventType==AccessibilityEvent.TYPE_WINDOWS_CHANGED || event.eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){syncTarget()}
  // Generic subtree/content notifications never label normal video motion as BLOCK.
 }
 @Suppress("DEPRECATION")
 private fun findTarget():Target? {
  val root=rootInActiveWindow?:return null
  if(!isTargetPackage(root.packageName?.toString())){root.recycle();return null}
  val rootPackageForLayoutIsSynthetic=root.packageName?.toString()=="com.augelab.visualfilter.synthetic.scroll"
  val id=root.windowId;val bounds=Rect()
  val window=windows.firstOrNull{it.id==id};if(window!=null)window.getBoundsInScreen(bounds)else root.getBoundsInScreen(bounds)
  if(bounds.width()<=0 || bounds.height()<=0){root.recycle();return null}
  val queue=ArrayDeque<AccessibilityNodeInfo>();queue.add(root);val candidates=mutableListOf<MediaCandidate>();var visited=0;val classes=mutableMapOf<String,Int>();val largeContainers=mutableListOf<String>()
  try{while(queue.isNotEmpty() && visited<384){val node=queue.removeFirst();try{
   visited++;val cls=node.className?.toString().orEmpty()
   if(repo?.developer()==true){val safe=cls.replace(Regex("[^A-Za-z0-9_.$]"),"_").take(120);classes[safe]=(classes[safe]?:0)+1}
   if(repo?.developer()==true && node.isVisibleToUser && largeContainers.size<32){
    val r=Rect();node.getBoundsInScreen(r)
    if(r.width()>=bounds.width()*.60 && r.height()>=bounds.height()*.12){
     val resource=node.viewIdResourceName?.replace(Regex("[^A-Za-z0-9_.$:/]"),"_")?.take(160)
     largeContainers.add("class=$cls,id=$resource,children=${node.childCount},bounds=$r")
    }
   }
   if(node.isVisibleToUser && isMediaNode(cls,node.viewIdResourceName)){
    val r=Rect();node.getBoundsInScreen(r);candidates.add(MediaCandidate(cls,PixelRect(r.left,r.top,r.right,r.bottom),node.viewIdResourceName,"$id:${node.hashCode()}:${node.viewIdResourceName}"))
   }
   for(i in 0 until minOf(node.childCount,64)){node.getChild(i)?.let{queue.add(it)}}
  }finally{node.recycle()}}}finally{while(queue.isNotEmpty())queue.removeFirst().recycle()}
  val pixels=PixelRect(bounds.left,bounds.top,bounds.right,bounds.bottom)
  val layout=mediaLayout(pixels,candidates,repo!!.region(),allowGeneric=rootPackageForLayoutIsSynthetic)
  if(repo?.developer()==true){
   val geometry="window=$id visited=$visited classes=${classes.toSortedMap()} candidates=${candidates.map{it.bounds}} selected=${layout.rectangles} automatic=${layout.automatic} large_containers=$largeContainers"
   if(geometry!=lastLayoutDiagnostic){lastLayoutDiagnostic=geometry;Log.i("VisualFilterScroll","event=layout_metadata $geometry")}
  }
  return Target(id,pixels,layout)
 }
 private fun syncTarget():Target? {
  if(!running)return null
  val now=findTarget();target=now
  if(now==null || now.layout.rectangles.isEmpty()){
   gate.leave();mask?.hide()
   if(ready)SessionState.set(UiStatus(true,if(now==null)"Filtre etkin; Instagram görseli bekleniyor." else "Görsel sınırı bulunamadı. Bu görünümde maske uygulanamıyor.",SessionState.state.value.metrics))
   return null
  }
  val key="${now.bounds}|${now.layout.rectangles}|${now.layout.tracks.map{it.identity}}"
  gate.updateTrackedWindow(now.id,key,now.layout.tracks)
  render();return now
 }
 private fun render(){target?.let{
  val threshold=model?.threshold?:BinaryModelContract.THRESHOLD
  val labels=if(repo?.developer()==true)gate.scores.mapIndexed{index,score->if(score==null)""else "Son kare: %.1f%% | Eşik: %.1f%%\nİlk engel: %s\nBu içerik için engel korunuyor".format(score*100,threshold*100,gate.blockScores.getOrNull(index)?.let{"%.1f%%".format(it*100)}?:"—")}else emptyList()
  mask?.show(it.bounds,it.layout.rectangles,gate.states,labels,gate.moments)
 }}
 private fun tick(){
  val now=syncTarget()?:return
  if(reviewOrder(now.bounds,now.layout.rectangles,gate.states,repo?.developer()==true).isEmpty())return
  if(Build.VERSION.SDK_INT>=34 && ready && !busy && captureRetry.remainingMs(SystemClock.uptimeMillis())==0L)capture(now)
 }
 /** Completion drives throughput. The100ms watcher only refreshes geometry/idle targets. */
 private fun requestNextCapture(){
  if(!running || !ready || busy || capturePump?.isActive==true)return
  val session=lifecycle
  val job=scope.launch(start=CoroutineStart.LAZY){
   yield() // Return control to main/UI dispatch; never recursively invoke a capture callback.
   val wait=captureRetry.remainingMs(SystemClock.uptimeMillis());if(wait>0)delay(wait)
   capturePump=null
   if(running && lifecycle==session){try{tick()}catch(e:Exception){if(e is CancellationException)throw e;recoverFiltering("İnceleme kesildi: ${e.javaClass.simpleName}")}}
  }
  capturePump=job;job.start()
 }
 @android.annotation.TargetApi(34)
 private fun capture(snapshot:Target){
  val token=gate.generation;val session=lifecycle;val classifier=model?:return
  val order=reviewOrder(snapshot.bounds,snapshot.layout.rectangles,gate.states,repo?.developer()==true)
  busy=true;val request=++captureRequests;captureRetry.requested(SystemClock.uptimeMillis())
  if(repo?.developer()==true)Log.i("VisualFilterScroll","event=capture_requested request=$request epoch=$token rate_mode=max request_elapsed_ns=${SystemClock.elapsedRealtimeNanos()}")
  takeScreenshotOfWindow(snapshot.id,mainExecutor,object:TakeScreenshotCallback {
   override fun onFailure(errorCode:Int){
    if(session!=lifecycle)return
    busy=false;if(!running)return
    if(repo?.developer()==true)Log.i("VisualFilterScroll","event=capture_failure android_code=$errorCode epoch=$token stale=${token!=gate.generation}")
    val rateLimited=errorCode==ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT
    captureRetry.failed(SystemClock.uptimeMillis(),rateLimited)
    syncTarget();if(!rateLimited && token==gate.generation){gate.scrolled(preserveBlocks=true);render()}
    SessionState.set(UiStatus(true,if(rateLimited)"Android yakalama sınırı; yeniden deneniyor."else "Pencere incelemesi bekliyor (Android kodu$errorCode). Önceki engel kararı korunuyor.",SessionState.state.value.metrics))
    requestNextCapture()
   }
   override fun onSuccess(screenshot:ScreenshotResult){
    val acquired=SystemClock.elapsedRealtimeNanos()
    if(running && session==lifecycle)captureRetry.success()
    if(!running || session!=lifecycle || token!=gate.generation){screenshot.hardwareBuffer.close();if(session==lifecycle){busy=false;requestNextCapture()};stale++;return}
    if(repo?.developer()==true)Log.i("VisualFilterScroll","event=capture_acquired request=$request epoch=$token acquired_elapsed_ns=$acquired")
    scope.launch{
     var full:Bitmap?=null
     try{
      full=withContext(Dispatchers.Default){
       screenshot.hardwareBuffer.use{buffer->val hardware=Bitmap.wrapHardwareBuffer(buffer,screenshot.colorSpace)?:error("Screenshot buffer unavailable");try{(hardware.copy(Bitmap.Config.ARGB_8888,false)?:error("Bitmap conversion failed")).also{full=it}}finally{hardware.recycle()}}
      }
      val source=full!!
      for(index in order){
       val rectangle=snapshot.layout.rectangles[index]
       if(!running || session!=lifecycle || token!=gate.generation){stale++;break}
       val timed=withContext(Dispatchers.Default){
        val local=Rect(((rectangle.left-snapshot.bounds.left).toLong()*source.width/snapshot.bounds.width).toInt(),((rectangle.top-snapshot.bounds.top).toLong()*source.height/snapshot.bounds.height).toInt(),((rectangle.right-snapshot.bounds.left).toLong()*source.width/snapshot.bounds.width).toInt(),((rectangle.bottom-snapshot.bounds.top).toLong()*source.height/snapshot.bounds.height).toInt())
        val bitmap=Bitmap.createBitmap(source,local.left,local.top,local.width(),local.height())
        try{val begin=SystemClock.elapsedRealtimeNanos();val answer=classifier.classify(bitmap);Triple(begin,answer,SystemClock.elapsedRealtimeNanos())}finally{if(bitmap!==source)bitmap.recycle()}
       }
       syncTarget()
       if(!running || session!=lifecycle || !gate.accept(token,index,timed.second.shouldBlock,timed.second.blockScore)){stale++;break}
       render()
       if(gate.states[index]==ReviewState.BLOCK && gate.moments[index]==null && mask?.covers(rectangle)==true){
        val moment=JourneyMoment(repo!!.recordBlock(),SystemClock.uptimeMillis())
        check(gate.markCounted(token,index,moment));render()
        if(repo?.developer()==true)Log.i("VisualFilterScroll","event=journey_progress total=${moment.total} level=${moment.level} released=${moment.released} epoch=$token region=$index scope=observed_blocked_encounter")
       }
       val dispatch=SystemClock.elapsedRealtimeNanos();completed++
       val answer=timed.second;val mode="Tespit edilen görsel sınırları"
       val detail="Son karar: ${answer.category} | NSFW %.3f / eşik %.5f | $mode | ${gate.states.joinToString()} | karar$completed | eski sonuç$stale | model %.0fms | otomatik maksimum hız".format(answer.blockScore,classifier.threshold,answer.inferenceTimeMs)
       SessionState.set(UiStatus(true,"Engellenen içerik ekrandan ayrılana kadar kapalı kalır.",detail))
       if(repo?.developer()==true)Log.i("VisualFilterScroll","epoch=$token region=$index target_package=${if(snapshot.layout.tracks.any{it.identity?.contains("com.instagram.android:id/")==true})INSTAGRAM else "debug_fixture_or_unknown"} tracked_move=${gate.retainedBlockDuringMove} rate_mode=max request=$request automatic_bounds=${snapshot.layout.automatic} masked_regions=${gate.states.count{it==ReviewState.BLOCK}} overlay_attached=${mask?.attached==true} T0_source=unknown T0_present=unknown screenshot_uptime_ms=${screenshot.timestamp} scroll_event_uptime_ms=$lastScrollUptimeMs T1_elapsed_ns=$acquired T2_elapsed_ns=${timed.first} T3_elapsed_ns=${timed.third} T4_dispatch_elapsed_ns=$dispatch T4_present=unknown blocked=${answer.shouldBlock} classifier=mobilenetv3small224_review_e202e26c nsfw_probability=${answer.blockScore} stages_ms=${answer.stageMs} selected_bounds=${rectangle}")
      }
     }catch(e:CancellationException){throw e}catch(e:Exception){
      if(running && session==lifecycle){gate.scrolled(preserveBlocks=true);render();SessionState.set(UiStatus(true,"İnceleme başarısız; önceki engel kararı korunuyor (${e.javaClass.simpleName})."));captureRetry.failed(SystemClock.uptimeMillis(),false)}
     }finally{full?.recycle();if(session==lifecycle){busy=false;requestNextCapture()}}
    }.invokeOnCompletion{screenshot.hardwareBuffer.close()}
   }
  })
 }
 companion object { const val INSTAGRAM="com.instagram.android" }
 private fun isTargetPackage(name:String?):Boolean=name==INSTAGRAM || (isEmulatorDebugBuild(this) && name=="com.augelab.visualfilter.synthetic.scroll")
}

/** Test target is allowed only in debug on x86_64, never in the ARM64 phone release. */
fun isEmulatorDebugBuild(context:android.content.Context):Boolean=
 context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE !=0 && Build.SUPPORTED_ABIS.contains("x86_64")
