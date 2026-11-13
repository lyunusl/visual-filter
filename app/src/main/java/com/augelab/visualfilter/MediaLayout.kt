package com.augelab.visualfilter
data class PixelRect(val left:Int,val top:Int,val right:Int,val bottom:Int) {
 val width get()=right-left;val height get()=bottom-top
 fun clipped(b:PixelRect):PixelRect?{val r=PixelRect(maxOf(left,b.left),maxOf(top,b.top),minOf(right,b.right),minOf(bottom,b.bottom));return r.takeIf{it.width>0 && it.height>0}}
}
data class MediaCandidate(val className:String,val bounds:PixelRect,val resourceName:String?=null,val identity:String?=null)
data class MediaLayout(val rectangles:List<PixelRect>,val automatic:Boolean,val tracks:List<TrackedMedia> = emptyList())
/** Exact photo/video IDs observed on the local Instagram emulator. Generic feed containers are excluded. */
fun isInstagramMediaResource(resourceName:String?):Boolean =
 resourceName in setOf("com.instagram.android:id/carousel_image","com.instagram.android:id/carousel_image_media_group",
  "com.instagram.android:id/row_feed_photo_imageview","com.instagram.android:id/media_group","com.instagram.android:id/video_container",
  "com.instagram.android:id/clips_video_container")
fun isMediaNode(className:String,resourceName:String?):Boolean =
 className in setOf("android.widget.ImageView","android.view.TextureView","android.view.SurfaceView") || isInstagramMediaResource(resourceName)
/** Geometry only. Unsupported or ambiguous layouts remain visible. */
fun mediaLayout(window:PixelRect,candidates:List<MediaCandidate>,fallback:MediaRegion,allowGeneric:Boolean=true):MediaLayout {
 require(window.width>0 && window.height>0)
 val rectangles=candidates.filter{isMediaNode(it.className,it.resourceName) && (allowGeneric || isInstagramMediaResource(it.resourceName) || it.className in setOf("android.view.TextureView","android.view.SurfaceView"))}.mapNotNull{it.bounds.clipped(window)}
  .filter{it.width>=window.width*.60 && it.height>=8}
  .distinct().sortedBy{it.width.toLong()*it.height}
 val selected=mutableListOf<PixelRect>()
 for(r in rectangles){if(selected.none{o->o.left>=r.left && o.top>=r.top && o.right<=r.right && o.bottom<=r.bottom})selected.add(r)}
 if(selected.isNotEmpty() && selected.size<=3)return selected.sortedBy{it.top}.let{ordered->MediaLayout(ordered,true,tracks=ordered.map{r->
  val node=candidates.firstOrNull{it.bounds.clipped(window)==r && isMediaNode(it.className,it.resourceName)}
  TrackedMedia(node?.identity,r)
 })}
 return MediaLayout(emptyList(),false)
}

/** Keep an existing mask on a departing sliver, but do not classify tiny cropped content. */
fun isReviewableMedia(window:PixelRect,media:PixelRect):Boolean=media.height>=window.height*.12
