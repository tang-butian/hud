package com.shilapi.xcertplay.menglong;
import android.graphics.*;
import android.text.*;
/** Dark rounded card and vector maneuver arrow, driven only by real navigation metadata. */
public final class GaodeCard {
 private static final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
 public static void draw(Canvas c,RectF r,int code,String distance,String road){
  draw(c,r,code,distance,road,true);
 }
 public static void drawTransparent(Canvas c,RectF r,int code,String distance,String road){draw(c,r,code,distance,road,false);}
 private static void draw(Canvas c,RectF r,int code,String distance,String road,boolean background){
  if(r.width()<=0||r.height()<=0)return;int save=c.save();c.clipRect(r);if(background){paint.setStyle(Paint.Style.FILL);paint.setColor(NightTheme.isNight()?0xf0141820:0xf2222930);float radius=Math.min(r.width(),r.height())*.12f;c.drawRoundRect(r,radius,radius,paint);}
  float pad=r.width()*.08f,arrowSize=Math.min(r.width()*.16f,r.height()*.30f),top=r.top+r.height()*.17f;
  arrow(c,new RectF(r.left+pad,top,r.left+pad+arrowSize,top+arrowSize),code);
  float x=r.left+pad+arrowSize+r.width()*.055f,width=r.right-pad-x,px=Math.min(r.height()*.25f,width/7f);
  text(c,distance==null||distance.isEmpty()?HudFormat.direction(code):distance,x,top+px*.9f,px,width,true);
  String second=NavText.road(code,road);
  if(code==0&&(distance==null||distance.isEmpty()))second="等待导航指引";
  text(c,second,r.left+pad,r.top+r.height()*.76f,Math.min(r.height()*.19f,(r.width()-pad*2)/10f),r.width()-pad*2,false);
  c.restoreToCount(save);
 }
 private static void text(Canvas c,String s,float x,float y,float px,float width,boolean bold){paint.setStyle(Paint.Style.FILL);paint.setColor(NightTheme.isNight()?0xffdfe7ef:Color.WHITE);paint.setTextAlign(Paint.Align.LEFT);paint.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);paint.setTextSize(Math.max(1,px));c.drawText(TextUtils.ellipsize(s,new TextPaint(paint),Math.max(1,width),TextUtils.TruncateAt.END).toString(),x,y,paint);}
 private static void arrow(Canvas c,RectF r,int code){int save=c.save();c.translate(r.left,r.top);c.scale(r.width(),r.height());paint.setColor(NightTheme.isNight()?0xffdfe7ef:Color.WHITE);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(.12f);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeCap(Paint.Cap.SQUARE);Path p=new Path();
  if(code==48){c.drawCircle(.5f,.5f,.25f,paint);c.drawLine(.5f,.81f,.5f,1,paint);}
  else if(code==13||(code>=25&&code<=44)){c.drawArc(new RectF(.17f,.12f,.84f,.78f),35,290,false,paint);p.moveTo(.70f,.08f);p.lineTo(.9f,.12f);p.lineTo(.85f,.34f);c.drawPath(p,paint);c.drawLine(.5f,.81f,.5f,1,paint);}
  else if(code==9||code==10){p.moveTo(.75f,.95f);p.lineTo(.75f,.36f);p.cubicTo(.75f,0,.2f,0,.2f,.36f);p.lineTo(.2f,.72f);p.moveTo(.03f,.55f);p.lineTo(.2f,.73f);p.lineTo(.37f,.55f);c.drawPath(p,paint);}
  else {boolean left=code==1||code==7,right=code==2||code==8,diagonal=code==3||code==4;
   if(left||right){float end=left?.06f:.94f,start=left?.78f:.22f;p.moveTo(start,.95f);p.lineTo(start,.30f);p.lineTo(end,.30f);p.moveTo(left?.29f:.71f,.06f);p.lineTo(end,.30f);p.lineTo(left?.29f:.71f,.54f);}
   else {if(diagonal)c.rotate(code==3?-40:40,.5f,.5f);p.moveTo(.5f,.95f);p.lineTo(.5f,.08f);p.moveTo(.25f,.34f);p.lineTo(.5f,.08f);p.lineTo(.75f,.34f);}c.drawPath(p,paint);}
  paint.setStyle(Paint.Style.FILL);c.restoreToCount(save);
 }
}
