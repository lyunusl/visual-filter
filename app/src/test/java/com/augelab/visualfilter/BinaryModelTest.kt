package com.augelab.visualfilter
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest
class BinaryModelTest {
 @Test fun userThresholdChangesDecisionWithoutChangingProbabilities(){
  val scores=floatArrayOf(.4f,.6f)
  assertTrue(BinaryModelContract.blocks(scores,.3f));assertFalse(BinaryModelContract.blocks(scores,.6f))
  assertArrayEquals(floatArrayOf(.4f,.6f),scores,0f)
 }
 @Test fun invalidUserThresholdsAreRejected(){
  for(t in listOf(Float.NaN,Float.POSITIVE_INFINITY,0f,1f))assertThrows(IllegalArgumentException::class.java){BinaryModelContract.blocks(floatArrayOf(.4f,.6f),t)}
 }
 @Test fun firstIndexIsBlockingAndThresholdIsInclusive(){
  assertTrue(BinaryModelContract.blocks(floatArrayOf(.8f,.2f)))
  assertFalse(BinaryModelContract.blocks(floatArrayOf(.1f,.9f)))
  val threshold=BinaryModelContract.THRESHOLD
  assertEquals(0.4477543234825134f,threshold,0f)
  assertTrue(BinaryModelContract.blocks(floatArrayOf(threshold,1f-threshold)))
  val below=Math.nextDown(threshold)
  assertFalse(BinaryModelContract.blocks(floatArrayOf(below,1f-below)))
  assertFalse(BinaryModelContract.blocks(floatArrayOf(.3f,.7f)))
 }
 @Test fun invalidOutputCannotBecomeNormal(){
  for(scores in listOf(floatArrayOf(1f),floatArrayOf(Float.NaN,0f),floatArrayOf(2f,-1f),floatArrayOf(.1f,.1f)))assertThrows(IllegalArgumentException::class.java){BinaryModelContract.blocks(scores)}
 }
 @Test fun letterboxPreservesAspectAndTiesToEven(){
  assertEquals(Letterbox(224,38,0,93),BinaryImageInput.layout(224,38))
  assertEquals(Letterbox(224,64,0,80),BinaryImageInput.layout(896,258))
  assertEquals(Letterbox(64,224,80,0),BinaryImageInput.layout(258,896))
 }
 @Test fun rgbOrderRawRangeAndGrayPadding(){
  val input=BinaryImageInput.prepare(IntArray(224*64){0xff0a64fa.toInt()},224,64)
  assertArrayEquals(floatArrayOf(128f,128f,128f),input.sliceArray(0..2),0f)
  assertArrayEquals(floatArrayOf(10f,100f,250f),input.sliceArray(80*224*3 until 80*224*3+3),0f)
 }
 @Test fun pillowPreparationMatchesAllSyntheticPixelHashes(){
  val stream=javaClass.getResourceAsStream("/binary-preprocess-goldens.tsv")!!
  stream.bufferedReader().useLines{lines->lines.forEach{line->
   val row=line.split('\t');val w=row[0].toInt();val h=row[1].toInt()
   val pixels=IntArray(w*h){i->val x=i%w;val y=i/w;(255 shl 24) or (((x*17+y*3)%256) shl 16) or (((x*5+y*29)%256) shl 8) or ((x*11+y*7)%256)}
   val prepared=BinaryImageInput.prepare(pixels,w,h);val bytes=ByteArray(prepared.size){prepared[it].toInt().toByte()}
   val hash=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
   assertEquals("${w}x$h",row[2],hash)
  }}
 }
}
