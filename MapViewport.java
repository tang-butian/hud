package com.shilapi.xcertplay.menglong;
/** Content dimensions depend on the saved reference, never the current viewport. */
public final class MapViewport {
    private MapViewport(){}
    public static int percent(int value){return Math.max(40,Math.min(140,value));}
    public static int[] render(int referenceW,int referenceH,double aspect,boolean crop,int percent){
        int[] base=render(referenceW,referenceH,aspect,crop);float factor=percent(percent)/100f;
        return new int[]{Math.max(1,Math.round(base[0]*factor)),Math.max(1,Math.round(base[1]*factor))};
    }
    public static int[] render(int referenceW,int referenceH,double aspect,boolean crop){
        if(!Double.isFinite(aspect)||aspect<=0)aspect=16.0/9.0;
        float unit=LayoutGeometry.scale(Math.max(1,referenceW),Math.max(1,referenceH),aspect,crop);
        return new int[]{Math.max(1,Math.round((float)(unit*aspect))),Math.max(1,Math.round(unit))};
    }
}
