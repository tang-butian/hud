package com.shilapi.xcertplay.menglong;

/** Pixel-distance masks; no phone data, Android APIs, or video frame sampling. */
public final class MapShape {
 public static float[] preset(int shape){
  if(shape==2)return new float[]{.20f,0,.80f,0,.91f,.25f,1,1,.70f,1,.30f,1,0,1,.09f,.25f};
  return new float[]{.10f,0,.90f,0,1,.10f,1,.90f,.90f,1,.10f,1,0,.90f,0,.10f};
 }
 public static float clamp(float x){return Float.isFinite(x)?Math.max(0,Math.min(1,x)):0;}
 private static double segment(double x,double y,double ax,double ay,double bx,double by){
  double dx=bx-ax,dy=by-ay,len=dx*dx+dy*dy;
  double t=len==0?0:Math.max(0,Math.min(1,((x-ax)*dx+(y-ay)*dy)/len));
  return Math.hypot(x-ax-t*dx,y-ay-t*dy);
 }
 public static float distance(int shape,float[] points,float x,float y,int width,int height){
  if(width<1||height<1||!Float.isFinite(x)||!Float.isFinite(y))return -1;
  if(shape==0){
   double radius=Math.min(width,height)*.15,dx=Math.abs(x-width*.5)-(width*.5-radius),dy=Math.abs(y-height*.5)-(height*.5-radius);
   return (float)(radius-Math.hypot(Math.max(dx,0),Math.max(dy,0))-Math.min(Math.max(dx,dy),0));
  }
  if(shape==1){
   double rx=width*.5,ry=height*.5;
   // Smooth interior ellipse fade; center and boundary are exact.
   return (float)((1-Math.hypot((x-rx)/rx,(y-ry)/ry))*Math.min(rx,ry));
  }
  if(points==null||points.length!=16)return -1;
  boolean inside=false;double nearest=Double.POSITIVE_INFINITY;
  for(int i=0,j=7;i<8;j=i++){
   double ax=clamp(points[2*j])*width,ay=clamp(points[2*j+1])*height,bx=clamp(points[2*i])*width,by=clamp(points[2*i+1])*height;
   if((ay>y)!=(by>y)&&x<(bx-ax)*(y-ay)/(by-ay)+ax)inside=!inside;
   nearest=Math.min(nearest,segment(x,y,ax,ay,bx,by));
  }
  return (float)(inside?nearest:-nearest);
 }
 public static int alpha(float distance,float feather){
  if(!Float.isFinite(distance)||distance<=0)return 0;
  if(feather<=0)return 255;
  float t=Math.min(1,distance/feather);return Math.round(255*t*t*(3-2*t));
 }
}
