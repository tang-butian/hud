package com.shilapi.xcertplay.menglong;

import android.content.Context;
import android.content.SharedPreferences;

/** Neutral defaults retain the tested direct HUD output until enhancement is selected. */
public final class InstrumentNightDetail {
    public final float contrast, sharpness;
    public final boolean bold;
    public InstrumentNightDetail(Context c) {
        SharedPreferences p=c.getSharedPreferences("menglong_hud_214",0);
        contrast=Math.max(60,Math.min(180,p.getInt("map_contrast",120)))/100f;
        sharpness=Math.max(0,Math.min(100,p.getInt("map_sharpness",35)))/100f;
        bold=p.getBoolean("map_bold",true);
    }
    public InstrumentNightDetail(float contrast,float sharpness,boolean bold){this.contrast=contrast;this.sharpness=sharpness;this.bold=bold;}
    public boolean enabled(){return Math.abs(contrast-1)>.001f||sharpness>0||bold;}
}
