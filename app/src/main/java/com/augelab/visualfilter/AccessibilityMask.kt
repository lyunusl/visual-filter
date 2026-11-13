package com.augelab.visualfilter
import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
/** Pending/unknown results stay visible; windows are physically bounded to each image. */
fun blockedMedia(bounds:PixelRect,rectangles:List<PixelRect>,decisions:List<ReviewState>):List<PixelRect> =
 rectangles.mapIndexedNotNull{i,r->if(decisions.getOrNull(i)==ReviewState.BLOCK)r.clipped(bounds)else null}
class AccessibilityMask(private val service:AccessibilityService){
 private val wm=service.getSystemService(WindowManager::class.java)
 private val views=mutableListOf<JourneyMaskView>();private var shown:List<PixelRect> = emptyList()
 private var shownLabels:List<String> = emptyList()
 private var shownMedia:List<PixelRect> = emptyList()
 private var shownMoments:List<JourneyMoment?> = emptyList()
 val attached:Boolean get()=views.isNotEmpty()
 fun covers(media:PixelRect)=attached && shown.any{it.clipped(media)==media}
 fun show(bounds:PixelRect,rectangles:List<PixelRect>,decisions:List<ReviewState>,labels:List<String> = emptyList(),moments:List<JourneyMoment?> = emptyList()){
  // One opaque surface per media: no control holes, fragmented windows or fade-in.
  val selected=blockedMedia(bounds,rectangles,decisions)
  val selectedLabels=maskLabels(selected,rectangles,labels)
  val primary=maskLabels(selected,rectangles,List(rectangles.size){"primary"})
  if(selected==shown && selectedLabels==shownLabels && rectangles==shownMedia && moments==shownMoments)return
  if(selected.isEmpty()){hide();return}
  try{for((index,r) in selected.withIndex()){
   val parameters=WindowManager.LayoutParams(r.width,r.height,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.OPAQUE).apply{gravity=Gravity.TOP or Gravity.START;x=r.left;y=r.top;windowAnimations=0;if(Build.VERSION.SDK_INT>=30)setFitInsetsTypes(0)}
   val existing=views.getOrNull(index)
   val owner=rectangles.indexOfFirst{r.clipped(it)==r};val whole=rectangles.getOrNull(owner)?:r
   if(existing==null){val view=JourneyMaskView(service);view.bind(r,whole,primary[index].isNotEmpty(),selectedLabels[index],moments.getOrNull(owner));wm.addView(view,parameters);views.add(view)}
   else{if(shown.getOrNull(index)!=r)wm.updateViewLayout(existing,parameters);existing.bind(r,whole,primary[index].isNotEmpty(),selectedLabels[index],moments.getOrNull(owner))}
  }
  while(views.size>selected.size){wm.removeViewImmediate(views.removeAt(views.lastIndex))}
  if(selected!=shown && selectedLabels.any{it.isNotEmpty()})android.util.Log.i("VisualFilterScroll","event=mask_geometry surfaces=${views.size} blocked_media=${selected.size} contiguous=true bounds=$selected")
  shown=selected;shownLabels=selectedLabels;shownMedia=rectangles;shownMoments=moments
  if(selectedLabels.any{it.isNotEmpty()})android.util.Log.i("VisualFilterScroll","event=debug_mask_labels visible_labels=${selectedLabels.count{it.isNotEmpty()}} text=${selectedLabels.filter{it.isNotEmpty()}.joinToString(";").replace('\n',' ')}")
  }catch(e:Exception){hide();throw e}
 }
 fun hide(){views.forEach{wm.removeViewImmediate(it)};views.clear();shown=emptyList();shownLabels=emptyList();shownMedia=emptyList();shownMoments=emptyList()}
 fun close(){hide()}
}

/** One label per media; never expands beyond that media. */
fun maskLabels(pieces:List<PixelRect>,media:List<PixelRect>,labels:List<String>):List<String>{
 val result=MutableList(pieces.size){""}
 media.forEachIndexed{i,r->
  val largest=pieces.indices.filter{pieces[it].clipped(r)==pieces[it]}.maxByOrNull{pieces[it].width.toLong()*pieces[it].height}
  if(largest!=null)result[largest]=labels.getOrNull(i).orEmpty()
 }
 return result
}
