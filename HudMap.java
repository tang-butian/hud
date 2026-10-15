package com.shilapi.xcertplay.menglong;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.FrameLayout;
import com.shilapi.xcertplay.MapMirrors;
import com.shilapi.xcertplay.menglong.LayoutGeometry.Box;
/** Direct decoded output of CarPlay map stream 111. No screenshots or instrument dependency. */
public final class HudMap extends FrameLayout implements TextureView.SurfaceTextureListener {
 private final String id;private final Box area;private final int percent;private final FrameLayout viewport;private final TextureView texture;
 private final Handler main=new Handler(Looper.getMainLooper());private Surface surface;private SurfaceTexture bound;private boolean stopped;private int renderW,renderH;private long frames,lastFrame;private boolean styleNight;private HudMapEnhancer enhancer;
 public static volatile String status="尚未打开 HUD 地图";
 public HudMap(Context c,String key,Box area,int percent,boolean mirror){super(c);id="menglong-hud-map-"+key+"-"+System.identityHashCode(this);this.area=area;this.percent=HudFocus.percent(percent);setBackgroundColor(Color.TRANSPARENT);setClipChildren(true);if(mirror)setScaleX(-1);viewport=new V3HudFrame(c);viewport.setClipChildren(true);addView(viewport,new FrameLayout.LayoutParams(1,1));texture=new TextureView(c);styleNight=NightTheme.isNight();texture.setOpaque(true);texture.setSurfaceTextureListener(this);viewport.addView(texture,new FrameLayout.LayoutParams(1,1,Gravity.CENTER));setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
 @Override protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);resize();}
 private void resize(){if(stopped||getWidth()<1||getHeight()<1)return;int[] a=LayoutGeometry.pixels(area,getWidth(),getHeight());FrameLayout.LayoutParams box=(FrameLayout.LayoutParams)viewport.getLayoutParams();if(box.width!=a[2]||box.height!=a[3]||box.leftMargin!=a[0]||box.topMargin!=a[1]){box.width=a[2];box.height=a[3];box.leftMargin=a[0];box.topMargin=a[1];viewport.setLayoutParams(box);}int[] render=V3HudCrop.active(getContext())?MapViewport.render(a[2],a[3],MapMirrors.INSTANCE.getStreamAspect(),false,100):HudFocus.render(a[2],a[3],MapMirrors.INSTANCE.getStreamAspect(),percent);if(renderW!=render[0]||renderH!=render[1]){renderW=render[0];renderH=render[1];texture.setLayoutParams(new FrameLayout.LayoutParams(renderW,renderH,Gravity.CENTER));}V3HudCrop.apply(texture);if(viewport instanceof V3HudFrame)((V3HudFrame)viewport).refresh();}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();stopped=false;main.removeCallbacks(tick);main.post(tick);}
 private final Runnable tick=new Runnable(){public void run(){if(stopped)return;if(!UiDiagnostics.guard(getContext(),"HUD 独立地图布局",()->{if(enhancer!=null)enhancer.setNight(NightTheme.isNight());if(styleNight!=NightTheme.isNight()){styleNight=NightTheme.isNight();texture.setOpaque(true);}resize();if(surface==null){SurfaceTexture available=texture.getSurfaceTexture();if(available!=null&&texture.isAvailable())bind(available);}refreshStatus();})){stop();return;}main.postDelayed(this,1000);}};
 private void refreshStatus(){if(surface==null)status="等待 HUD 地图 Surface";else if(!MapMirrors.INSTANCE.getStreamActive())status="等待 CarPlay 独立地图流，启用后请重新连接";else if(frames==0)status="独立地图流已启动，等待 HUD 解码帧";else if(android.os.SystemClock.elapsedRealtime()-lastFrame>3000)status="HUD 已收到地图帧，当前等待更新";else status="HUD 独立地图实时输出 · "+renderW+"×"+renderH;}
 private void bind(SurfaceTexture t){if(stopped||t==bound&&surface!=null)return;release();bound=t;resize();surface=new Surface(t);frames=0;lastFrame=0;MapMirrors.INSTANCE.set(id,surface);refreshStatus();}
 public void stop(){stopped=true;main.removeCallbacksAndMessages(null);release();status="HUD 地图已关闭";}
 private void release(){UiDiagnostics.guard(getContext(),"移除 HUD 独立地图 Surface",()->MapMirrors.INSTANCE.set(id,null));if(enhancer!=null){enhancer.close();enhancer=null;}Surface old=surface;surface=null;bound=null;frames=0;lastFrame=0;if(old!=null)UiDiagnostics.guard(getContext(),"释放 HUD Surface",()->old.release());}
 @Override protected void onDetachedFromWindow(){stop();super.onDetachedFromWindow();}
 public void onSurfaceTextureAvailable(SurfaceTexture t,int w,int h){UiDiagnostics.guard(getContext(),"HUD 独立地图绑定",()->bind(t));}
 public boolean onSurfaceTextureDestroyed(SurfaceTexture t){if(bound==t)release();return true;}
 public void onSurfaceTextureSizeChanged(SurfaceTexture t,int w,int h){UiDiagnostics.guard(getContext(),"HUD 独立地图尺寸",()->resize());}
 public void onSurfaceTextureUpdated(SurfaceTexture t){if(stopped||bound!=t)return;frames++;lastFrame=android.os.SystemClock.elapsedRealtime();if(frames==1)refreshStatus();}
}
