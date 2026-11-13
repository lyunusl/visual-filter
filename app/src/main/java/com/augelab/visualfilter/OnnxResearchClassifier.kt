package com.augelab.visualfilter

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import java.io.File
import java.nio.FloatBuffer
import java.security.MessageDigest

/** Offline, single-worker experimental binary classifier; model files only are copied to app-private storage.
 * No image, segmentation map or screenshot is persisted or sent to another process/service.
 */
class OnnxResearchClassifier(context:Context,val threshold:Float=BinaryModelContract.THRESHOLD):ContentClassifier {
 init{require(BinaryModelContract.validThreshold(threshold))}
 private val app=context.applicationContext
 private val closed=java.util.concurrent.atomic.AtomicBoolean(false)
 private val env=OrtEnvironment.getEnvironment().apply{setTelemetry(false)}
 private fun file(name:String,hash:String):File {
  val directory=File(app.filesDir,"research_models").apply{mkdirs()}
  val target=File(directory,name)
  fun digest(f:File):String{val md=MessageDigest.getInstance("SHA-256");f.inputStream().use{stream->val chunk=ByteArray(65536);while(true){val n=stream.read(chunk);if(n<0)break;md.update(chunk,0,n)}};return md.digest().joinToString(""){"%02x".format(it)}}
  if(target.isFile && digest(target)==hash)return target
  val temporary=File(directory,"$name.pending")
  try{app.assets.open(name).use{input->temporary.outputStream().use{input.copyTo(it)}};require(digest(temporary)==hash){"Bundled model checksum mismatch"};require(temporary.renameTo(target)){"Model cache update failed"}}finally{temporary.delete()}
  return target
 }
 private fun session(name:String,hash:String):OrtSession {
  val path=file(name,hash).absolutePath
  return OrtSession.SessionOptions().use{configuration->
   configuration.setIntraOpNumThreads(2);configuration.setInterOpNumThreads(1)
   env.createSession(path,configuration)
  }
 }
 private val lock=Any()
 private val binary=lazy{
  session(BinaryModelContract.FILE,BinaryModelContract.SHA256).also{s->
   try{
    val input=s.inputInfo.getValue(BinaryModelContract.INPUT).info as ai.onnxruntime.TensorInfo
    val output=s.outputInfo.getValue(BinaryModelContract.OUTPUT).info as ai.onnxruntime.TensorInfo
    require(s.inputNames==setOf(BinaryModelContract.INPUT) && s.outputNames==setOf(BinaryModelContract.OUTPUT))
    require(input.type==ai.onnxruntime.OnnxJavaType.FLOAT && input.shape.contentEquals(longArrayOf(1,BinaryModelContract.SIZE.toLong(),BinaryModelContract.SIZE.toLong(),3)))
    require(output.type==ai.onnxruntime.OnnxJavaType.FLOAT && output.shape.contentEquals(longArrayOf(1,2)))
   }catch(e:Exception){s.close();throw e}
  }
 }
 fun prepareInput(bitmap:Bitmap):FloatArray {
  val pixels=IntArray(bitmap.width*bitmap.height)
  bitmap.getPixels(pixels,0,bitmap.width,0,0,bitmap.width,bitmap.height)
  return BinaryImageInput.prepare(pixels,bitmap.width,bitmap.height)
 }
 fun warmUp(){val bitmap=Bitmap.createBitmap(BinaryModelContract.SIZE,BinaryModelContract.SIZE,Bitmap.Config.ARGB_8888);try{bitmap.eraseColor(0xff808080.toInt());evaluate(bitmap)}finally{bitmap.recycle()}}
 private fun evaluate(bitmap:Bitmap):ClassificationResult=synchronized(lock){
  check(!closed.get()){"Classifier closed"}
  val start=SystemClock.elapsedRealtimeNanos();val input=prepareInput(bitmap);val prepared=SystemClock.elapsedRealtimeNanos()
  val probabilities=OnnxTensor.createTensor(env,FloatBuffer.wrap(input),longArrayOf(1,BinaryModelContract.SIZE.toLong(),BinaryModelContract.SIZE.toLong(),3)).use{tensor->
   binary.value.run(mapOf(BinaryModelContract.INPUT to tensor)).use{result->
    val output=result.get(BinaryModelContract.OUTPUT).get() as OnnxTensor
    require(output.info.shape.contentEquals(longArrayOf(1,2)))
    FloatArray(2).also{output.floatBuffer.get(it)}
   }
  }
  val end=SystemClock.elapsedRealtimeNanos();val blocked=BinaryModelContract.blocks(probabilities,threshold)
  ClassificationResult(if(blocked)"NSFW_KISKIRTICI" else "NORMAL",probabilities.max(),blocked,(end-start)/1e6,probabilities,probabilities[0],mapOf("preprocess" to (prepared-start)/1e6,"onnx" to (end-prepared)/1e6))
 }
 override suspend fun classify(bitmap:Bitmap):ClassificationResult=evaluate(bitmap)
 override fun close(){synchronized(lock){if(closed.compareAndSet(false,true) && binary.isInitialized())binary.value.close()}}
}
