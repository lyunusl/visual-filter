package com.augelab.visualfilter
import android.graphics.Bitmap
data class ClassificationResult(val category:String,val confidence:Float,val shouldBlock:Boolean,val inferenceTimeMs:Double,val scores:FloatArray,val blockScore:Float,val stageMs:Map<String,Double> = emptyMap())
interface ContentClassifier:AutoCloseable { suspend fun classify(bitmap:Bitmap):ClassificationResult;override fun close(){} }
