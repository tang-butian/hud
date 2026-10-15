package com.shilapi.xcertplay.menglong;
public final class V3CropGeometry {
 private V3CropGeometry(){}
 public static float[] transform(float l,float t,float r,float b,int width,int height){if(!Float.isFinite(l)||!Float.isFinite(t)||!Float.isFinite(r)||!Float.isFinite(b)||l<0||t<0||r>1||b>1||r-l<.02f||b-t<.02f)throw new IllegalArgumentException("Invalid crop");return new float[]{1/(r-l),1/(b-t),-l*Math.max(1,width)/(r-l),-t*Math.max(1,height)/(b-t)};}
}
