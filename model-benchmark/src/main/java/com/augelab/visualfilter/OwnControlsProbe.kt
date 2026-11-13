package com.augelab.visualfilter
import android.app.Instrumentation
import android.app.Activity
import android.app.UiAutomation
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject

/** Reads only our own control UI; no screenshots, grants, gestures or Instagram nodes. */
class OwnControlsProbe:Instrumentation(){
 override fun onCreate(arguments:Bundle?){super.onCreate(arguments);start()}
 override fun onStart(){
  val out=Bundle()
  try{
   check(targetContext.packageName=="com.augelab.visualfilter.benchmark")
   val automation=getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
   val root=automation.rootInActiveWindow?:error("No own UI")
   check(root.packageName?.toString()=="com.augelab.visualfilter.research"){"Only own app controls may be read"}
   val rows=JSONArray()
   fun visit(n:AccessibilityNodeInfo){
    try{
     check(n.packageName?.toString()=="com.augelab.visualfilter.research")
     val r=Rect();n.getBoundsInScreen(r)
     if(n.isVisibleToUser)rows.put(JSONObject().put("text",if(n.isPassword)""else n.text?.toString().orEmpty()).put("class",n.className?.toString().orEmpty()).put("bounds","[${r.left},${r.top}][${r.right},${r.bottom}]").put("enabled",n.isEnabled.toString()).put("clickable",n.isClickable.toString()).put("checkable",n.isCheckable.toString()).put("checked",n.isChecked.toString()))
     for(i in 0 until n.childCount)n.getChild(i)?.let{visit(it)}
    }finally{n.recycle()}
   }
   visit(root);out.putString("controls",rows.toString());finish(Activity.RESULT_OK,out)
  }catch(e:Exception){out.putString("error",e.javaClass.simpleName+":"+e.message);finish(Activity.RESULT_CANCELED,out)}
 }
}
