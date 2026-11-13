package com.augelab.visualfilter.synthetic

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.FrameLayout
import android.widget.Button
import android.view.Gravity

/** Offline licensed fixtures in real ImageView nodes; no network, user content, screenshots or grants. */
class SyntheticActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  val density=resources.displayMetrics.density
  fun dp(value:Int)=(value*density).toInt()
  val column=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.WHITE);setPadding(dp(12),dp(12),dp(12),dp(24))}
  fun label(value:String){column.addView(TextView(this).apply{text=value;textSize=18f;setTextColor(Color.BLACK);setPadding(0,dp(12),0,dp(12))})}
  label("OFFLINE TEST — Instagram değildir. Lisanslı test görselleri.")
  label("Kahve uygun; bikini engel adayı. Görsel üstünden yukarı kaydırın. Yazı/düğme alanları görünür kalmalı.")
  val persistentProbe=intent.getBooleanExtra("persistent_probe",false)
  val fixtures=if(persistentProbe)listOf("bikini_contest","coffee","coffee","coffee")else listOf("coffee","bikini_beach","coffee","bikini_contest","coffee","leggings_fitness","coffee")
  fixtures.forEachIndexed{index,id->
   label("TEST ${index+1}: $id")
   val image=object:ImageView(this){private var first=true
    override fun onDraw(canvas:Canvas){
     if(first){Log.i("VisualFilterSynthetic","scene=$index fixture=$id T0_draw_start_elapsed_ns=${SystemClock.elapsedRealtimeNanos()} T0_present=unknown source=licensed_fixture");first=false}
     super.onDraw(canvas)
    }
   }.apply{
    scaleType=ImageView.ScaleType.FIT_CENTER
    setBackgroundColor(Color.rgb(242,242,242))
    assets.open("$id.png").use{setImageBitmap(BitmapFactory.decodeStream(it))}
    importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES
    contentDescription="Offline test image ${index+1}"
   }
   if(intent.getBooleanExtra("overlay_probe",false)){
    val frame=FrameLayout(this)
    frame.addView(image,FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT))
    frame.addView(Button(this).apply{text="TEST BUTTON";setBackgroundColor(Color.MAGENTA)},FrameLayout.LayoutParams(dp(100),dp(40),Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply{topMargin=dp(100)})
    column.addView(frame,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(350)))
   }else column.addView(image,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(350)))
   if(persistentProbe && index==0)image.postDelayed({
    if(!isDestroyed){assets.open("coffee.png").use{image.setImageBitmap(BitmapFactory.decodeStream(it))}
     Log.i("VisualFilterSynthetic","event=same_node_safe_fixture source=licensed_fixture")}
   },16000)
   label("Görsel ${index+1} altındaki bu yazı maskelenmemeli.")
  }
  val scroll=ScrollView(this).apply{
   addView(column)
   setOnScrollChangeListener{_,_,y,_,oldY->Log.i("VisualFilterSynthetic","scroll_y=$y previous_y=$oldY event_elapsed_ns=${SystemClock.elapsedRealtimeNanos()}")}
  }
  setContentView(scroll)
 }
}
