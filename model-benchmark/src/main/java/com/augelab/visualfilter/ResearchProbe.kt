package com.augelab.visualfilter
import android.app.Instrumentation
import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.security.MessageDigest
import android.os.Bundle
import android.os.SystemClock
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
/** Numerical parity only, licensed fixtures/generated RGB. No live capture. */
class ResearchProbe:Instrumentation(){
 override fun onCreate(arguments:Bundle?){super.onCreate(arguments);start()}
 override fun onStart(){
  val rows=JSONArray();val setup=SystemClock.elapsedRealtimeNanos();val model=OnnxResearchClassifier(targetContext);var thresholdControlChecked=false
  try{
   model.warmUp();val warm=(SystemClock.elapsedRealtimeNanos()-setup)/1e6
   val reference=JSONObject(context.assets.open("binary-parity.json").bufferedReader().use{it.readText()})
   require(reference.getString("model_sha256")==BinaryModelContract.SHA256)
   require(reference.getDouble("threshold").toFloat()==BinaryModelContract.THRESHOLD)
   val fixtures=reference.getJSONArray("rows")
   runBlocking{
    for(i in 0 until fixtures.length()){
     val f=fixtures.getJSONObject(i);val bitmap=if(f.getString("kind")=="licensed_fixture"){
      context.assets.open(f.getString("fixture_file")).use{BitmapFactory.decodeStream(it)}?:error("Unreadable fixture")
     }else{
      val w=f.getInt("width");val h=f.getInt("height")
      val pixels=IntArray(w*h){j->val x=j%w;val y=j/w;(255 shl 24) or (((x*17+y*3)%256) shl 16) or (((x*5+y*29)%256) shl 8) or ((x*11+y*7)%256)}
      Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888)
     }
     try{
      val prepared=model.prepareInput(bitmap);val bytes=ByteArray(prepared.size){prepared[it].toInt().toByte()}
      val hash=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
      require(hash==f.getString("tensor_rgb_sha256")){"Preprocess pixel mismatch: "+f.getString("id")}
      val times=mutableListOf<Double>();val stages=JSONArray();var answer:ClassificationResult?=null
      repeat(if(i==0)3 else 1){val start=SystemClock.elapsedRealtimeNanos();answer=model.classify(bitmap);times.add((SystemClock.elapsedRealtimeNanos()-start)/1e6);stages.put(JSONObject(answer!!.stageMs))}
      val a=answer!!;val expected=f.getJSONArray("probabilities")
      val difference=(0..1).maxOf{kotlin.math.abs(a.scores[it].toDouble()-expected.getDouble(it))}
      require(difference<=reference.getDouble("probability_atol")){"Inference parity mismatch: "+f.getString("id")}
      require(a.shouldBlock==f.getBoolean("blocked"))
      if(!thresholdControlChecked && a.blockScore in .051f.. .949f){
       val lower=OnnxResearchClassifier(targetContext,.05f)
       val lo=try{lower.classify(bitmap)}finally{lower.close()}
       val upper=OnnxResearchClassifier(targetContext,.95f)
       val hi=try{upper.classify(bitmap)}finally{upper.close()}
       require(lo.shouldBlock && !hi.shouldBlock);require(kotlin.math.abs(lo.blockScore-hi.blockScore)<.0001f);thresholdControlChecked=true
      }
      rows.put(JSONObject().put("id",f.getString("id")).put("blocked",a.shouldBlock).put("probabilities",JSONArray(a.scores.toList())).put("pixel_hash_match",true).put("max_probability_difference",difference).put("trial_stage_ms",stages).put("pipeline_ms",JSONArray(times)).put("process_pss_kb",android.os.Debug.getPss()).put("native_heap_allocated_bytes",android.os.Debug.getNativeHeapAllocatedSize()))
     }finally{bitmap.recycle()}
    }
    require(thresholdControlChecked){"No threshold control case evaluated"}
    model.close();model.close()
    val bitmap=Bitmap.createBitmap(BinaryModelContract.SIZE,BinaryModelContract.SIZE,Bitmap.Config.ARGB_8888)
    try{var rejected=false;try{model.classify(bitmap)}catch(e:IllegalStateException){rejected=true};require(rejected){"Closed session accepted inference"}}finally{bitmap.recycle()}
    val reopened=OnnxResearchClassifier(targetContext);try{reopened.warmUp()}finally{reopened.close()}
   }
   finish(Activity.RESULT_OK,Bundle().apply{putString("records",rows.toString());putDouble("model_setup_warmup_ms",warm);putBoolean("close_reject_reopen_passed",true);putBoolean("threshold_control_passed",thresholdControlChecked);putString("stream","BINARY_MODEL_PROBE_PASSED")})
  }catch(e:Exception){finish(Activity.RESULT_CANCELED,Bundle().apply{putString("stream","BINARY_MODEL_PROBE_FAILED ${e.javaClass.simpleName}: ${e.message}")})}
  finally{model.close()}
 }
}
