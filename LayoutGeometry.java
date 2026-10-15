package com.shilapi.xcertplay.menglong;

/** Normalized display rectangles; independent of Android for verification. */
public final class LayoutGeometry {
    public static final class Box {
        public float l,t,r,b;
        public Box(float l,float t,float r,float b){this.l=l;this.t=t;this.r=r;this.b=b;}
        public Box copy(){return new Box(l,t,r,b);}
        public float width(){return r-l;} public float height(){return b-t;}
    }
    private static float clamp(float n,float lo,float hi){return Math.max(lo,Math.min(hi,n));}
    public static Box valid(Box a){
        if(!Float.isFinite(a.l)||!Float.isFinite(a.t)||!Float.isFinite(a.r)||!Float.isFinite(a.b))return new Box(.05f,.05f,.95f,.95f);
        float l=clamp(a.l,0,.95f),t=clamp(a.t,0,.95f);
        return new Box(l,t,clamp(a.r,l+.05f,1),clamp(a.b,t+.05f,1));
    }
    public static Box move(Box a,float dx,float dy){dx=clamp(dx,-a.l,1-a.r);dy=clamp(dy,-a.t,1-a.b);return new Box(a.l+dx,a.t+dy,a.r+dx,a.b+dy);}
    /** Edge bits: left=1, top=2, right=4, bottom=8; zero moves the box. */
    public static Box resize(Box a,int edges,float dx,float dy){
        if(edges==0)return move(a,dx,dy);
        return new Box((edges&1)!=0?clamp(a.l+dx,0,a.r-.05f):a.l,
            (edges&2)!=0?clamp(a.t+dy,0,a.b-.05f):a.t,
            (edges&4)!=0?clamp(a.r+dx,a.l+.05f,1):a.r,
            (edges&8)!=0?clamp(a.b+dy,a.t+.05f,1):a.b);
    }
    /** Rounded physical pixel edges, guaranteeing at least one output pixel. */
    public static int[] pixels(Box a,int w,int h){a=valid(a);int l=Math.round(a.l*w),t=Math.round(a.t*h);int r=Math.min(w,Math.max(l+1,Math.round(a.r*w))),b=Math.min(h,Math.max(t+1,Math.round(a.b*h)));return new int[]{l,t,r-l,b-t};}
    public static float scale(int w,int h,double aspect,boolean crop){
        if(w<1||h<1||aspect<=0||!Double.isFinite(aspect))return 1;
        return (float)(crop?Math.max(w/aspect,h):Math.min(w/aspect,h));
    }
}
