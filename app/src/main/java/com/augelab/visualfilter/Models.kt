package com.augelab.visualfilter

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ClassificationResult(val category: String, val confidence: Float, val shouldBlock: Boolean, val inferenceTimeMs: Double,val scores:FloatArray,val blockScore:Float,val stageMs:Map<String,Double> = emptyMap())
interface ContentClassifier : AutoCloseable { suspend fun classify(bitmap: Bitmap): ClassificationResult; override fun close() {} }
class FilterRepository(context: Context) {
 private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
 fun hasStopPin()=prefs.contains("stop_pin_record")
 fun createStopPin(pin:String):Boolean=synchronized(pinLock){
  if(hasStopPin())false else prefs.edit().putString("stop_pin_record",StopPin.create(pin)).commit()
 }
 fun verifyStopPin(pin:String):Boolean=synchronized(pinLock){
  val now=System.currentTimeMillis()
  val attempts=PinAttempts(prefs.getInt("pin_failures",0),prefs.getLong("pin_wait_until",0))
  if(attempts.waitMillis(now)>0)return@synchronized false
  val valid=StopPin.verify(pin,prefs.getString("stop_pin_record","").orEmpty())
  val next=if(valid)PinAttempts()else attempts.failed(now)
  val saved=prefs.edit().putInt("pin_failures",next.failures).putLong("pin_wait_until",next.blockedUntil).commit()
  valid && saved
 }
 fun pinWaitSeconds()=((PinAttempts(0,prefs.getLong("pin_wait_until",0)).waitMillis(System.currentTimeMillis())+999)/1000)
 fun filterRequested()=prefs.getBoolean("filter_requested",false)
 fun blockCount()=prefs.getLong("blocked_encounters",0).coerceAtLeast(0)
 fun recordBlock():Long=synchronized(progressLock){nextBlockTotal(blockCount()).also{prefs.edit().putLong("blocked_encounters",it).apply()}}
 fun observeProgress(onChange:()->Unit):AutoCloseable{
  val listener=android.content.SharedPreferences.OnSharedPreferenceChangeListener{_,key->if(key=="blocked_encounters")onChange()}
  prefs.registerOnSharedPreferenceChangeListener(listener)
  return AutoCloseable{prefs.unregisterOnSharedPreferenceChangeListener(listener)}
 }
 fun setFilterRequested(value:Boolean)=prefs.edit().putBoolean("filter_requested",value).commit()
 companion object { private val pinLock=Any();private val progressLock=Any();private val thresholdLock=Any() }
 fun threshold():Float=synchronized(thresholdLock){
  val current=if(prefs.contains(BinaryModelContract.THRESHOLD_KEY))prefs.getFloat(BinaryModelContract.THRESHOLD_KEY,Float.NaN)else null
   val previousKeys=listOf("threshold_mobilenetv3large_review_epoch1_528ffddd","threshold_adamcodd_review_ft_epoch6_40fa6c3c","threshold_marqo_vit_tiny_384_ft_epoch3_f79f1027","binary_block_threshold")
   val legacy=previousKeys.firstOrNull{prefs.contains(it)}?.let{prefs.getFloat(it,Float.NaN)}
  val choice=ModelThresholdPolicy.choose(current,legacy)
  if(choice.initializeCurrentModel)prefs.edit().putFloat(BinaryModelContract.THRESHOLD_KEY,choice.value).putBoolean("threshold_notice_"+BinaryModelContract.MODEL_ID,choice.migratedFromLegacy).apply()
  choice.value
 }
 fun modelThresholdMigrated()=prefs.getBoolean("threshold_notice_"+BinaryModelContract.MODEL_ID,false)
 fun setThreshold(value:Float){require(BinaryModelContract.validThreshold(value));prefs.edit().putFloat(BinaryModelContract.THRESHOLD_KEY,value).putBoolean("threshold_notice_"+BinaryModelContract.MODEL_ID,false).apply()}
 fun region():MediaRegion=runCatching{MediaRegion(prefs.getFloat("roi_left",0f),prefs.getFloat("roi_top",.08f),prefs.getFloat("roi_right",.90f),prefs.getFloat("roi_bottom",.78f))}.getOrDefault(MediaRegion())
 fun setRegion(r:MediaRegion){prefs.edit().putFloat("roi_left",r.left).putFloat("roi_top",r.top).putFloat("roi_right",r.right).putFloat("roi_bottom",r.bottom).apply()}
 fun developer(): Boolean = prefs.getBoolean("developer", false)
 fun setDeveloper(value: Boolean) { prefs.edit().putBoolean("developer",value).apply() }
}
data class UiStatus(val active:Boolean = false, val message:String = "Stopped", val metrics:String = "")
object SessionState {
 private val mutable = MutableStateFlow(UiStatus())
 val state = mutable.asStateFlow()
 fun set(status:UiStatus) { mutable.value=status }
}
