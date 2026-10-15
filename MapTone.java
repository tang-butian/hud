package com.shilapi.xcertplay.menglong;

/** Shallow light-background key, applied before edge masks; no video readback. */
public final class MapTone {
 public static float[] matrix(int color,boolean key,int threshold,int transition){
  float[] m=new float[]{((color>>16)&255)/255f,0,0,0,0,0,((color>>8)&255)/255f,0,0,0,0,0,(color&255)/255f,0,0,0,0,0,1,0};
  if(key){float gain=255f/Math.max(8,Math.min(80,transition));float edge=Math.max(224,Math.min(255,threshold));m[15]=m[16]=m[17]=-gain/3f;m[18]=gain*edge/255f;}
  return m;
 }
 /** Recover red-dominant source pixels without changing the ordinary light-background key.
  * Neutral white/gray and cyan produce zero alpha; original RGB is kept.
  * A small dead band avoids tinting neutral antialiasing or codec noise. */
 public static float[] redAccent(){
  float gain=255f/12f;
  return new float[]{1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,gain,-gain/2f,-gain/2f,-gain*4f/255f,0};
 }
 public static int alpha(float[] m,int red,int green,int blue,int alpha){
  return Math.max(0,Math.min(255,Math.round(m[15]*red+m[16]*green+m[17]*blue+m[18]*alpha+m[19])));
 }
 public static float topFade(float y,float height,int percent){
  if(percent<=0)return 1;if(height<=0||!Float.isFinite(y))return 0;
  float t=Math.max(0,Math.min(1,y/(height*Math.max(0,Math.min(40,percent))/100f)));return t*t*(3-2*t);
 }
}
