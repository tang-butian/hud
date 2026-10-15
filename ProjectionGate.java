package com.shilapi.xcertplay.menglong;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import java.lang.ref.WeakReference;
import com.shilapi.xcertplay.glance.CarPlayGlance;
/** Session gate independent of foreground visibility. Never reads video pixels. */
public final class ProjectionGate {
 private static WeakReference<Activity> host=new WeakReference<Activity>(null);
 private static final Handler handler=new Handler(Looper.getMainLooper());private static boolean shown;
 public static boolean isHost(Activity a){return a!=null&&a.getClass().getName().endsWith("CarPlayHostActivity");}
 public static void resumed(Activity a){if(!isHost(a))return;if(host.get()!=a){clear();shown=false;host=new WeakReference<Activity>(a);}handler.removeCallbacks(tick);handler.post(tick);}
 // onPause means background, not a disconnected session.
 public static void paused(Activity a){}
 public static void destroyed(Activity a){if(host.get()!=a)return;host.clear();handler.removeCallbacks(tick);shown=false;clear();}
 public static void clear(){InstrumentOutput.clear();HudOutput.clear();AppOutput.clear();}
 public static boolean visible(Activity a){if(a==null||host.get()!=a||a.isFinishing()||a.isDestroyed())return false;CarPlayGlance.Snapshot s=CarPlayGlance.INSTANCE.snapshot();return s!=null&&s.getConnected();}
 private static final Runnable tick=new Runnable(){public void run(){Activity a=host.get();if(a==null){clear();shown=false;return;}
  if(!UiDiagnostics.guard(a,"CarPlay 会话与后台仪表",()->{boolean current=visible(a);if(!current){clear();shown=false;}else if(!shown){shown=true;InstrumentOutput.resumed(a);}})){clear();shown=false;}
  handler.removeCallbacks(this);handler.postDelayed(this,200);
 }};
}
