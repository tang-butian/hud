package com.shilapi.xcertplay.menglong;
import android.app.*;
import android.view.*;
import android.widget.*;
/** One compact entry; every additional setting is reached from this menu. */
public final class SettingsHub {
 public static void resumed(Activity a){IndependentAudioVolume.init(a);ViewGroup root=(ViewGroup)a.getWindow().getDecorView();for(String tag:new String[]{"menglong-layout-entry","menglong-hud-entry","menglong-app-entry","menglong-wireless-audio","menglong-display-diagnostic"}){View old=root.findViewWithTag(tag);if(old!=null&&old.getParent() instanceof ViewGroup)((ViewGroup)old.getParent()).removeView(old);}if(root.findViewWithTag("menglong-settings-hub")!=null)return;Button b=new Button(a);b.setText("投屏设置");b.setTextSize(12);b.setAllCaps(false);b.setTag("menglong-settings-hub");b.setOnClickListener(v->show(a));float density=a.getResources().getDisplayMetrics().density;FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(Math.round(96*density),Math.round(40*density),Gravity.CENTER_VERTICAL|Gravity.RIGHT);p.topMargin=0;p.rightMargin=Math.round(8*density);root.addView(b,p);}
 public static void show(Activity a){new AlertDialog.Builder(a).setTitle("投屏设置").setItems(new String[]{"仪表布局","HUD 布局","App 投屏","无线音频","故障记录","麦克风与微信录音","音乐 / 导航独立音量"},(d,index)->UiDiagnostics.guard(a,"打开投屏设置",()->{switch(index){case 0:InstrumentOutput.settings(a);break;case 1:HudOutput.settings(a);break;case 2:AppOutput.settings(a);break;case 3:AudioCompatibility.settings(a);break;case 4:UiDiagnostics.show(a);break;case 5:MicrophoneCompatibility.settings(a);break;case 6:IndependentAudioVolume.settings(a);break;}})).setNegativeButton("关闭",null).show();}
}
