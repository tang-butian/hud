package com.shilapi.xcertplay.menglong;
/** HUD output pixels only; does not change the shared CarPlay decoder or stream. */
public final class HudResolution {
 private HudResolution(){}
 public static int[] fit(int width,int height,int displayWidth,int displayHeight){
  int w=Math.max(1,width),h=Math.max(1,height);
  int maxW=Math.max(1,Math.min(480,displayWidth)),maxH=Math.max(1,Math.min(240,displayHeight));
  double scale=Math.min(1d,Math.min((double)maxW/w,(double)maxH/h));
  return new int[]{Math.max(1,(int)Math.floor(w*scale)),Math.max(1,(int)Math.floor(h*scale))};
 }
}
