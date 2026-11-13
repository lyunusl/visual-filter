package com.augelab.visualfilter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import kotlin.math.*

/** Opaque procedural scenes, clipped to media. No assets, sound, network or touch handlers. */
class JourneyMaskView(context:Context):View(context){
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 private val path=Path()
 private val oval=RectF()
 private val d=resources.displayMetrics.density
 private var piece=PixelRect(0,0,1,1);private var media=piece
 private var label="";private var primary=false
 private var moment=JourneyMoment(0,0)
 private var debugLayout:StaticLayout?=null;private var heading:StaticLayout?=null;private var body:StaticLayout?=null
 private var gradient:LinearGradient?=null
 private var animate=false
 private var messageSlot=-1L
 private var previewMoment:JourneyMoment?=null
 private val palettes=arrayOf(
  intArrayOf(0xff174652.toInt(),0xff102e38.toInt(),0xffffd591.toInt()),
  intArrayOf(0xff17395c.toInt(),0xff10283d.toInt(),0xff9fe2de.toInt()),
  intArrayOf(0xff244b3e.toInt(),0xff102d2d.toInt(),0xffd2e5a3.toInt()),
  intArrayOf(0xff303350.toInt(),0xff151f36.toInt(),0xffe6cfef.toInt()),
  intArrayOf(0xff3c4055.toInt(),0xff202d42.toInt(),0xffd4dbff.toInt()),
  intArrayOf(0xff514838.toInt(),0xff233b3e.toInt(),0xfff5dba5.toInt()))
 init{setBackgroundColor(0xff102e38.toInt());importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
 override fun isOpaque()=true
 fun preview(journey:JourneyMoment){previewMoment=journey;if(width>0 && height>0){val r=PixelRect(0,0,width,height);bind(r,r,true,"",journey)}}
 override fun onSizeChanged(w:Int,h:Int,oldw:Int,oldh:Int){super.onSizeChanged(w,h,oldw,oldh);previewMoment?.let{preview(it)}}
 fun bind(fragment:PixelRect,whole:PixelRect,showMessage:Boolean,debug:String,journey:JourneyMoment?){
  val next=journey?:JourneyMoment(0,0)
  val contentChanged=piece!=fragment || primary!=showMessage || moment!=next
  val sceneChanged=media!=whole || moment.theme!=next.theme
  val debugChanged=piece!=fragment || label!=debug
  piece=fragment;media=whole;primary=showMessage;label=debug;moment=next
  if(sceneChanged || gradient==null){val colors=palettes[moment.theme.ordinal];gradient=LinearGradient(0f,0f,0f,media.height.toFloat(),colors[0],colors[1],Shader.TileMode.CLAMP)}
  if(debugChanged)debugLayout=if(debug.isEmpty())null else textLayout(debug,11f,Color.WHITE,Layout.Alignment.ALIGN_NORMAL,5)
  if(contentChanged)messageSlot=-1
  invalidate()
 }
 private fun textLayout(text:String,size:Float,color:Int,alignment:Layout.Alignment,lines:Int):StaticLayout{
  val p=TextPaint(Paint.ANTI_ALIAS_FLAG).apply{this.color=color;textSize=size*d;typeface=Typeface.create("sans-serif",if(size>=18)Typeface.BOLD else Typeface.NORMAL)}
  return StaticLayout.Builder.obtain(text,0,text.length,p,maxOf(1,piece.width-(32*d).toInt())).setAlignment(alignment).setIncludePad(false).setMaxLines(lines).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
 }
 override fun onAttachedToWindow(){super.onAttachedToWindow();animate=ValueAnimator.areAnimatorsEnabled();invalidate()}
 override fun onDetachedFromWindow(){animate=false;super.onDetachedFromWindow()}
 override fun onDraw(canvas:Canvas){
  super.onDraw(canvas)
  animate=ValueAnimator.areAnimatorsEnabled()
  val slot=if(animate && moment.startedAtMs>0)(SystemClock.uptimeMillis()-moment.startedAtMs).coerceAtLeast(0)/15000 else 0
  if(slot!=messageSlot){
   messageSlot=slot
   val messages=JourneyMessages.all
   val message=messages[(((moment.total.coerceAtLeast(1)-1)%messages.size+slot%messages.size)%messages.size).toInt()]
   heading=textLayout(message.title,18f,0xfffff5dc.toInt(),Layout.Alignment.ALIGN_CENTER,2);body=textLayout(message.body,13f,0xffe0e9e5.toInt(),Layout.Alignment.ALIGN_CENTER,3)
  }
  val seconds=if(animate)(SystemClock.uptimeMillis()%120000L)/1000f else 0f
  val phase=sin(seconds*Math.PI/6).toFloat()
  val colors=palettes[moment.theme.ordinal];val w=media.width.toFloat();val h=media.height.toFloat()
  canvas.save();canvas.translate((media.left-piece.left).toFloat(),(media.top-piece.top).toFloat())
  paint.style=Paint.Style.FILL;paint.shader=gradient;canvas.drawRect(0f,0f,w,h,paint);paint.shader=null
  scene(canvas,w,h,seconds,phase,colors[2]);canvas.restore()
  val debugH=debugLayout?.let{it.height+20*d}?:0f
  val top=debugH+12*d;val available=height-top-14*d
  if(primary && width>=170*d && available>=90*d){
   smallText(canvas,"${moment.total} engelleme",width/2f,top+12*d,13f,colors[2])
   smallText(canvas,"SEVİYE ${moment.level} · ${moment.levelName}",width/2f,top+30*d,11f,0xffd4e3dd.toInt())
   val rich=available>=230*d
   if(rich){
    val radius=minOf(42*d,available*.15f,width*.15f)
    person(canvas,width/2f,top+available*.34f,radius,colors[2],seconds)
   }
   val textY=if(rich)top+available*.57f else top+42*d
   heading?.let{canvas.save();canvas.translate(16*d,textY);it.draw(canvas);canvas.restore()}
   val bodyY=textY+(heading?.height?:0)+7*d
   if(available>=150*d && bodyY+(body?.height?:0)<height-38*d)body?.let{canvas.save();canvas.translate(16*d,bodyY);it.draw(canvas);canvas.restore()}
   if(available>=160*d){
    val barY=height-29*d;val left=24*d;val span=width-48*d
    paint.style=Paint.Style.FILL;paint.color=0xff39545a.toInt();canvas.drawRoundRect(left,barY,left+span,barY+3*d,2*d,2*d,paint)
    paint.color=colors[2];canvas.drawRoundRect(left,barY,left+span*moment.released/10f,barY+3*d,2*d,2*d,paint)
    smallText(canvas,if(moment.released==10)"Seviye tamamlandı · Kaydırarak devam et"else "${moment.released}/10 halka açıldı · Kaydırarak devam et",width/2f,height-10*d,10f,0xffd4e3dd.toInt())
   }
  }
  debugLayout?.let{paint.style=Paint.Style.FILL;paint.color=0xff0a1f24.toInt();canvas.drawRect(0f,0f,width.toFloat(),debugH,paint);canvas.save();canvas.translate(16*d,10*d);it.draw(canvas);canvas.restore()}
  if(animate && isAttachedToWindow)postInvalidateDelayed(50)
 }
 private fun smallText(c:Canvas,text:String,x:Float,y:Float,size:Float,color:Int){
  paint.style=Paint.Style.FILL;paint.color=color;paint.textAlign=Paint.Align.CENTER;paint.typeface=Typeface.create("sans-serif",Typeface.NORMAL);paint.textSize=size*d
  val measured=paint.measureText(text);if(measured>width-24*d)paint.textSize*=((width-24*d)/measured)
  c.drawText(text,x,y,paint)
 }
 private fun scene(c:Canvas,w:Float,h:Float,t:Float,p:Float,accent:Int){
  paint.style=Paint.Style.FILL
  when(moment.theme){
   JourneyTheme.DAWN->{paint.color=0xff607c78.toInt();c.drawCircle(w*.78f,h*.18f,min(w,h)*(.10f+p*.004f),paint);hill(c,w,h,.72f,0xff31535a.toInt(),p*.008f);hill(c,w,h,.90f,0xff203f4c.toInt(),-p*.004f)}
   JourneyTheme.WAVES->{for(row in 0..4){paint.color=Color.argb(255,24+row*3,59+row*6,77+row*7);path.reset();path.moveTo(0f,h);for(i in 0..32){val x=w*i/32;val y=h*(.64f+row*.075f)+sin(i*.28f+t*.35f+row).toFloat()*h*.017f;path.lineTo(x,y)};path.lineTo(w,h);path.close();c.drawPath(path,paint)}}
   JourneyTheme.GROVE->{for(i in 0..6){val x=w*(i+.3f)/7;val sway=sin(t*.4f+i).toFloat()*w*.01f;paint.style=Paint.Style.STROKE;paint.strokeWidth=3*d;paint.color=0xff497762.toInt();path.reset();path.moveTo(x,h);path.quadTo(x+sway,h*.86f,x+sway*2,h*(.73f+(i%3)*.04f));c.drawPath(path,paint);paint.style=Paint.Style.FILL;oval.set(x-8*d+sway*2,h*.80f,x+8*d+sway*2,h*.80f+25*d);c.drawOval(oval,paint)};fireflies(c,w,h,t,accent)}
   JourneyTheme.STARS->{for(i in 0..19){val x=w*((i*37%97)+1)/100;val y=h*((i*23%83)+3)/100;paint.color=Color.argb(255,125+(i%3)*15,143+(i%3)*15,171+(i%3)*15);c.drawCircle(x,y,(1.1f+.35f*sin(t*.45f+i).toFloat())*d,paint)};paint.style=Paint.Style.STROKE;paint.strokeWidth=d;paint.color=0xff62657d.toInt();path.reset();path.moveTo(w*.10f,h*.15f);path.lineTo(w*.22f,h*.09f);path.lineTo(w*.36f,h*.17f);path.lineTo(w*.52f,h*.11f);c.drawPath(path,paint)}
   JourneyTheme.BREATH->{paint.style=Paint.Style.STROKE;paint.strokeWidth=2*d;for(i in 0..3){paint.color=Color.rgb(67+i*7,85+i*7,107+i*7);c.drawCircle(w/2,h*.40f,min(w,h)*(.15f+i*.08f+p*.012f),paint)}}
   JourneyTheme.PATH->{paint.style=Paint.Style.STROKE;paint.strokeWidth=w*.13f;paint.color=0xff3d5751.toInt();path.reset();path.moveTo(w*.15f,h);path.cubicTo(w*.90f,h*.78f,w*.12f,h*.40f,w*.77f,h*.16f);c.drawPath(path,paint);paint.style=Paint.Style.FILL;paint.color=0xffb4ae7d.toInt();c.drawCircle(w*.77f,h*.16f,min(w,h)*(.045f+p*.003f),paint);fireflies(c,w,h,t,accent)}
  }
  paint.style=Paint.Style.FILL
 }
 private fun fireflies(c:Canvas,w:Float,h:Float,t:Float,accent:Int){paint.color=accent;for(i in 0..5)c.drawCircle(w*(.1f+i*.16f)+sin(t*.3f+i).toFloat()*w*.015f,h*(.2f+(i%3)*.23f)+cos(t*.2f+i).toFloat()*h*.01f,1.5f*d,paint)}
 private fun hill(c:Canvas,w:Float,h:Float,level:Float,color:Int,shift:Float){path.reset();path.moveTo(0f,h*(level+shift));path.cubicTo(w*.3f,h*(level-.14f+shift),w*.6f,h*(level+.12f+shift),w,h*(level-.05f+shift));path.lineTo(w,h);path.lineTo(0f,h);path.close();paint.color=color;c.drawPath(path,paint)}
 private fun person(c:Canvas,x:Float,y:Float,r:Float,accent:Int,t:Float){
  paint.style=Paint.Style.FILL;paint.color=0xff233d48.toInt();c.drawCircle(x,y,r*1.33f,paint)
  paint.color=accent;c.drawCircle(x,y-r*.42f,r*.19f,paint)
  path.reset();path.moveTo(x-r*.27f,y-r*.10f);path.quadTo(x,y-r*.26f,x+r*.27f,y-r*.10f);path.lineTo(x+r*.38f,y+r*.50f);path.quadTo(x,y+r*.64f,x-r*.38f,y+r*.50f);path.close();c.drawPath(path,paint)
  paint.style=Paint.Style.STROKE;paint.strokeWidth=r*.08f;paint.strokeCap=Paint.Cap.ROUND
  c.drawLine(x-r*.24f,y,x-r*.48f,y-r*.19f,paint);c.drawLine(x+r*.24f,y,x+r*.48f,y-r*.19f,paint)
  for(i in 0..9){val angle=(-90+i*36)*Math.PI/180;val lx=x+cos(angle).toFloat()*r;val ly=y+sin(angle).toFloat()*r
   if(i>=moment.released){paint.color=0xff8aa8ac.toInt();c.save();c.rotate(i*36f,lx,ly);oval.set(lx-r*.10f,ly-r*.15f,lx+r*.10f,ly+r*.15f);c.drawOval(oval,paint);c.restore()}
   else{paint.style=Paint.Style.FILL;paint.color=accent;c.drawCircle(lx,ly,r*.038f,paint);paint.style=Paint.Style.STROKE}
  }
  val elapsed=if(animate && moment.startedAtMs>0)((SystemClock.uptimeMillis()-moment.startedAtMs)/2800f).coerceIn(0f,1f)else 1f
  if(moment.released>0 && elapsed<1){val angle=(-90+(moment.released-1)*36)*Math.PI/180;val distance=r*(1+elapsed*.75f);val lx=x+cos(angle).toFloat()*distance;val ly=y+sin(angle).toFloat()*distance;paint.color=accent;paint.alpha=((1-elapsed)*255).toInt();oval.set(lx-r*.12f,ly-r*.16f,lx+r*.12f,ly+r*.16f);c.drawArc(oval,25f,135f,false,paint);c.drawArc(oval,205f,135f,false,paint);paint.alpha=255}
  paint.style=Paint.Style.FILL
 }
}
