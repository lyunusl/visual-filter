package com.augelab.visualfilter

/** User-defined media rectangle. This is not automatic Instagram layout recognition. */
data class MediaRegion(val left:Float=0f,val top:Float=.08f,val right:Float=.90f,val bottom:Float=.78f){
 init{require(listOf(left,top,right,bottom).all{it.isFinite()} && left>=0 && top>=0 && right<=1 && bottom<=1 && right-left>=.09999f && bottom-top>=.09999f)}
 fun pixels(width:Int,height:Int):IntArray{
  require(width>0 && height>0)
  val l=(left*width).toInt().coerceIn(0,width-1);val t=(top*height).toInt().coerceIn(0,height-1)
  return intArrayOf(l,t,(right*width).toInt().coerceIn(l+1,width),(bottom*height).toInt().coerceIn(t+1,height))
 }
}

/** Identifies the app's opaque black mask only while already blocked. Never initiates masking. */
fun looksLikeOwnMask(pixels:IntArray):Boolean = pixels.isNotEmpty() && pixels.count{((it shr 16)and 255)<16 && ((it shr 8)and 255)<16 && (it and 255)<16}>=pixels.size*.90
