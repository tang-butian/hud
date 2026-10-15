package com.shilapi.xcertplay.menglong;
import com.shilapi.xcertplay.menglong.LayoutGeometry.Box;

/** Maps a normalized video source crop to a separate destination rectangle. */
public final class VideoRegion {
    public static final class Mapping {
        public final int[] source;
        public final float left,top,right,bottom;
        Mapping(int[] source,float left,float top,float right,float bottom){
            this.source=source;this.left=left;this.top=top;this.right=right;this.bottom=bottom;
        }
    }
    public static Mapping map(Box crop,int videoW,int videoH,int x,int y,int width,int height,boolean fill){
        if(videoW<1||videoH<1||width<1||height<1)throw new IllegalArgumentException("Empty video or destination");
        int[] s=LayoutGeometry.pixels(crop,videoW,videoH);
        s[0]=Math.max(0,Math.min(videoW-1,s[0]));s[1]=Math.max(0,Math.min(videoH-1,s[1]));
        s[2]=Math.max(1,Math.min(videoW-s[0],s[2]));s[3]=Math.max(1,Math.min(videoH-s[1],s[3]));
        double aspect=s[2]/(double)s[3];
        float h=LayoutGeometry.scale(width,height,aspect,fill),w=(float)(h*aspect);
        float l=x+(width-w)/2f,t=y+(height-h)/2f;
        return new Mapping(s,l,t,l+w,t+h);
    }
}
