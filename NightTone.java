package com.shilapi.xcertplay.menglong;
/** Night foreground extraction: neutral dark/mid-gray recedes, bright strokes and saturated colors remain. */
public final class NightTone {
 private static float[] identity(){return new float[]{1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0};}
 public static float[] clear(){return new float[]{.94f,0,0,0,18,0,.94f,0,0,18,0,0,.94f,0,18,0,0,0,1,0};}
 public static float[] luminance(){float[] m=identity();float gain=255f/64f;m[15]=m[16]=m[17]=gain/3f;m[18]=-gain*176f/255f;return m;}
 public static float[] chroma(int channel){float[] m=identity();float edge=channel==2?64f:16f,range=channel==2?48f:32f,gain=255f/range;for(int i=0;i<3;i++)m[15+i]=i==channel?gain:-gain/2f;m[18]=-gain*edge/255f;return m;}
 public static boolean scheduled(int minute,int start,int end){minute=Math.floorMod(minute,1440);start=Math.floorMod(start,1440);end=Math.floorMod(end,1440);return start==end||start<end&&minute>=start&&minute<end||start>end&&(minute>=start||minute<end);}
}
