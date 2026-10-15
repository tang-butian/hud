package com.shilapi.xcertplay.menglong;
import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Calendar;
/** Read the existing app controller; never change its state or infer night from video. */
public final class NightTheme {
 private static WeakReference<Activity> host=new WeakReference<Activity>(null);private static Method controllerGetter,nightGetter;private static boolean night;
 public static boolean isNight(){return night;}
 public static void refresh(Context c){
  if(c instanceof Activity&&c.getClass().getName().endsWith("CarPlayHostActivity"))host=new WeakReference<Activity>((Activity)c);
  Activity a=host.get();if(a!=null&&!a.isDestroyed())try{if(controllerGetter==null){controllerGetter=a.getClass().getDeclaredMethod("getNightModeController");controllerGetter.setAccessible(true);}Object controller=controllerGetter.invoke(a);if(nightGetter==null)nightGetter=controller.getClass().getMethod("getNight");night=(Boolean)nightGetter.invoke(controller);return;}catch(Exception ignored){}
  SharedPreferences p=c.getSharedPreferences("xcertplay_airplay",0);String mode=p.getString("carplay_night_mode","system");if("night".equals(mode)){night=true;return;}if("day".equals(mode)){night=false;return;}if("schedule".equals(mode)){Calendar now=Calendar.getInstance();int minute=now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE);night=NightTone.scheduled(minute,p.getInt("carplay_night_start_minute",1080),p.getInt("carplay_night_end_minute",360));return;}
  night=(c.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
 }
}
