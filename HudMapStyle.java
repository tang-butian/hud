package com.shilapi.xcertplay.menglong;

import android.content.Context;
import android.graphics.*;
import android.view.TextureView;
import android.widget.FrameLayout;

/** GPU-only HUD treatment of the existing decoded map. No frame capture or extra decoder. */
public final class HudMapStyle {
    private HudMapStyle() {}
    public static void apply(TextureView texture, boolean night) {
        texture.setOpaque(false);
        float[] values;
        if (night) {
            // Preserve night colours, suppress near-black background.
            values = new float[]{
                1,0,0,0,0, 0,1,0,0,0, 0,0,1,0,0,
                .897f,1.761f,.342f,0,-24
            };
        } else {
            // Light map: suppress pale land/sky, brighten dark roads while retaining route colour.
            values = new float[]{
                .402f,-1.174f,-.228f,0,255,
                -.598f,-.174f,-.228f,0,255,
                -.598f,-1.174f,.772f,0,255,
                -.897f,-1.761f,-.342f,0,705
            };
        }
        float gain=Math.max(100,Math.min(240,texture.getContext().getSharedPreferences("menglong_hud_214",0).getInt("map_brightness",100)))/100f;
        for(int i=0;i<values.length;i++)values[i]*=gain;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setColorFilter(new ColorMatrixColorFilter(values));
        texture.setLayerPaint(paint);
    }

    /** Soft boundaries only apply to the map, never to navigation or lyric overlays. */
    public static final class Viewport extends FrameLayout {
        private final Paint mask = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Shader horizontal, vertical;
        private final RectF bounds = new RectF();
        private final Path outline = new Path();
        public Viewport(Context c) {
            super(c); setBackgroundColor(Color.TRANSPARENT); setClipChildren(true);
            mask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
            super.onSizeChanged(w,h,oldw,oldh);
            bounds.set(0,0,w,h); outline.reset();
            float feather=Math.min(Math.max(1,Math.min(w,h)*.055f),Math.min(w,h)*.45f);
            outline.addRoundRect(bounds,feather,feather,Path.Direction.CW);
            if(w<1||h<1){horizontal=vertical=null;return;}
            int[] colors={Color.TRANSPARENT,Color.WHITE,Color.WHITE,Color.TRANSPARENT};
            horizontal=new LinearGradient(0,0,w,0,colors,new float[]{0,feather/w,1-feather/w,1},Shader.TileMode.CLAMP);
            vertical=new LinearGradient(0,0,0,h,colors,new float[]{0,feather/h,1-feather/h,1},Shader.TileMode.CLAMP);
        }
        @Override protected void dispatchDraw(Canvas c) {
            if(horizontal==null||vertical==null){super.dispatchDraw(c);return;}
            int layer=c.saveLayer(bounds,null);
            c.clipPath(outline);super.dispatchDraw(c);
            mask.setShader(horizontal);c.drawRect(bounds,mask);
            mask.setShader(vertical);c.drawRect(bounds,mask);
            c.restoreToCount(layer);
        }
    }
}
