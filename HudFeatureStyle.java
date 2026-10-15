package com.shilapi.xcertplay.menglong;
/** HUD feature light gain: background rejection is retained before gain. */
public final class HudFeatureStyle {
 private HudFeatureStyle(){}
 public static float[] matrix(boolean night,float gain){
  if(!Float.isFinite(gain))gain=1;
  gain=Math.max(1,Math.min(2.4f,gain));
  float[] v=night?new float[]{1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,.897f,1.761f,.342f,0,-24}:
   new float[]{.402f,-1.174f,-.228f,0,255,-.598f,-.174f,-.228f,0,255,-.598f,-1.174f,.772f,0,255,-.897f,-1.761f,-.342f,0,705};
  // Transparent land/sky remains zero; visible ink and route pixels become brighter/less faint.
  for(int i=0;i<v.length;i++)v[i]*=gain;
  return v;
 }
}
