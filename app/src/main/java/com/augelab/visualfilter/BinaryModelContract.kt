package com.augelab.visualfilter

/** Frozen local MobileNetV3 Small224 review fine-tune; normalization and softmax are inside this ONNX graph. */
object BinaryModelContract {
 const val MODEL_ID="mobilenetv3small224_review_e202e26c"
 const val THRESHOLD_KEY="threshold_"+MODEL_ID
 const val FILE="review_mobilenetv3small224_224_rgb_fp32.onnx"
 const val SHA256="e202e26cf35d3445d00ac7af74183d1d962121b3a5327fc41afd63605d5c4780"
 const val INPUT="rgb_0_255"
 const val OUTPUT="nsfw_normal_probabilities"
 const val SIZE=224
 const val THRESHOLD=0.4477543234825134f // Frozen validation-selected threshold; experimental labels.
 fun validThreshold(value:Float)=value.isFinite() && value in .05f.. .95f
 fun blocks(probabilities:FloatArray,threshold:Float=THRESHOLD):Boolean {
  require(validThreshold(threshold))
  require(probabilities.size==2 && probabilities.all{it.isFinite() && it in 0f..1f})
  require(kotlin.math.abs(probabilities.sum()-1f)<.001f)
  return probabilities[0]>=threshold
 }
}

data class Letterbox(val width:Int,val height:Int,val left:Int,val top:Int)

/** Input is already oriented RGB (Android window pixels have no EXIF).
 * Separable antialiased bilinear, rounded uint8 after each pass, matching Pillow RGB.
 * Reference: python-pillow/Pillow src/libImaging/Resample.c (22-bit coefficients).
 * Output NHWC raw RGB0..255 float; never divide255 or apply ImageNet a second time.
 */
object BinaryImageInput {
 private const val BITS=22
 fun layout(width:Int,height:Int):Letterbox {
  require(width>0 && height>0)
  val size=BinaryModelContract.SIZE
  val scale=size.toDouble()/maxOf(width,height)
  val w=maxOf(1,Math.rint(width*scale).toInt());val h=maxOf(1,Math.rint(height*scale).toInt())
  return Letterbox(w,h,(size-w)/2,(size-h)/2)
 }
 private data class Weights(val first:Int,val values:IntArray)
 private fun weights(input:Int,output:Int):List<Weights> {
  val scale=input.toDouble()/output;val support=maxOf(1.0,scale)
  return List(output){index->
   val center=(index+.5)*scale
   val first=maxOf(0,(center-support+.5).toInt());val end=minOf(input,(center+support+.5).toInt())
   val raw=DoubleArray(end-first){i->maxOf(0.0,1.0-kotlin.math.abs((i+first-center+.5)/support))}
   val sum=raw.sum();require(sum>0)
   Weights(first,IntArray(raw.size){i->(.5+raw[i]/sum*(1 shl BITS)).toInt()})
  }
 }
 private fun mix(pixels:IntArray,start:Int,stride:Int,weights:IntArray):Int {
  var r=1 shl (BITS-1);var g=r;var b=r
  for(i in weights.indices){val c=pixels[start+i*stride];val w=weights[i];r+=((c ushr 16)and 255)*w;g+=((c ushr 8)and 255)*w;b+=(c and 255)*w}
  return (255 shl 24) or ((r shr BITS).coerceIn(0,255) shl 16) or ((g shr BITS).coerceIn(0,255) shl 8) or (b shr BITS).coerceIn(0,255)
 }
 fun prepare(pixels:IntArray,width:Int,height:Int):FloatArray {
  require(width>0 && height>0 && pixels.size.toLong()==width.toLong()*height)
  val box=layout(width,height)
  val horizontal=if(box.width==width)pixels else {
   val wx=weights(width,box.width)
   IntArray(box.width*height){i->val w=wx[i%box.width];mix(pixels,(i/box.width)*width+w.first,1,w.values)}
  }
  val resized=if(box.height==height)horizontal else {
   val wy=weights(height,box.height)
   IntArray(box.width*box.height){i->val w=wy[i/box.width];mix(horizontal,w.first*box.width+i%box.width,box.width,w.values)}
  }
  val out=FloatArray(BinaryModelContract.SIZE*BinaryModelContract.SIZE*3){128f}
  for(y in 0 until box.height)for(x in 0 until box.width){
   val c=resized[y*box.width+x];val i=((box.top+y)*BinaryModelContract.SIZE+box.left+x)*3
   out[i]=((c ushr 16)and 255).toFloat();out[i+1]=((c ushr 8)and 255).toFloat();out[i+2]=(c and 255).toFloat()
  }
  return out
 }
}
