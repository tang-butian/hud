package com.shilapi.xcertplay.menglong;
/** Centered local viewport, independent from map coordinates and geographic zoom. */
public final class HudFocus {
 private HudFocus(){}
 public static int percent(int p){return Math.max(80,Math.min(180,p));}
 public static int[] render(int w,int h,double aspect,int p){int[] base=MapViewport.render(w,h,aspect,true);float scale=percent(p)/100f;return new int[]{Math.max(1,Math.round(base[0]*scale)),Math.max(1,Math.round(base[1]*scale))};}
 public static float background(int value){return Math.max(0,Math.min(100,value))/100f;}
 public static float alpha(float feature,float background){feature=Math.max(0,Math.min(1,feature));background=Math.max(0,Math.min(1,background));return feature+(1-feature)*background;}
}
