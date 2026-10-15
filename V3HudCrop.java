package com.shilapi.xcertplay.menglong;
import android.app.*;import android.content.*;import android.graphics.*;import android.view.*;import android.widget.*;
import com.shilapi.xcertplay.HudCrop;import com.shilapi.xcertplay.MapMirrors;import com.shilapi.xcertplay.glance.CarPlayGlance;
/** Independent navigation/cruise calibration over the original V3 crop transform. */
public final class V3HudCrop {
 private static SharedPreferences prefs(Context c){return c.getSharedPreferences("menglong_v3_hud_crop",0);}
 public static boolean navigating(){return CarPlayGlance.INSTANCE.isNavigationActive();}
 private static String prefix(boolean nav){return nav?"nav_":"cruise_";}
 public static boolean active(Context c){return prefs(c).getBoolean("enabled",false);}
 private static HudCrop read(Context c,boolean nav){SharedPreferences p=prefs(c);String k=prefix(nav);float l=p.getFloat(k+"l",p.getFloat("l",0)),t=p.getFloat(k+"t",p.getFloat("t",0)),r=p.getFloat(k+"r",p.getFloat("r",1)),b=p.getFloat(k+"b",p.getFloat("b",1));try{V3CropGeometry.transform(l,t,r,b,1,1);return new HudCrop(l,t,r,b);}catch(RuntimeException e){return new HudCrop(0,0,1,1);}}
 public static void apply(TextureView v){if(v.getWidth()<1||v.getHeight()<1)return;Matrix m=new Matrix();if(active(v.getContext())){HudCrop c=read(v.getContext(),navigating());float[] p=V3CropGeometry.transform(c.getLeft(),c.getTop(),c.getRight(),c.getBottom(),v.getWidth(),v.getHeight());m.setScale(p[0],p[1]);m.postTranslate(p[2],p[3]);}v.setTransform(m);}
 private static void write(SharedPreferences.Editor p,boolean nav,HudCrop c){String k=prefix(nav);p.putFloat(k+"l",c.getLeft()).putFloat(k+"t",c.getTop()).putFloat(k+"r",c.getRight()).putFloat(k+"b",c.getBottom());}
 public static void settings(Activity a){
  LinearLayout body=new LinearLayout(a);body.setOrientation(1);body.setPadding(16,12,16,12);
  TextView hint=new TextView(a);hint.setText("导航和巡航分别框选：拖动框内移动，拖边或角点缩放，让车辆图标落在框的中心十字。请在 iPhone 切换对应模式后校准。保存后按 CarPlay 导航状态自动切换；这里只改变 HUD 来源区域。");body.addView(hint);
  TextView state=new TextView(a);body.addView(state);
  final HudCrop[] drafts={read(a,true),read(a,false)};final int[] selected={navigating()?0:1};
  RadioGroup modes=new RadioGroup(a);modes.setOrientation(LinearLayout.HORIZONTAL);RadioButton nav=new RadioButton(a),cruise=new RadioButton(a);nav.setId(701);cruise.setId(702);nav.setText("导航位置");cruise.setText("巡航位置");modes.addView(nav);modes.addView(cruise);modes.check(selected[0]==0?701:702);body.addView(modes);
  FrameLayout frame=new FrameLayout(a);body.addView(frame,new LinearLayout.LayoutParams(-1,Math.round(240*a.getResources().getDisplayMetrics().density)));
  TextureView video=new TextureView(a);frame.addView(video,new FrameLayout.LayoutParams(-1,-1));Editor editor=new Editor(a,drafts[selected[0]]);frame.addView(editor,new FrameLayout.LayoutParams(-1,-1));
  modes.setOnCheckedChangeListener((g,id)->{drafts[selected[0]]=editor.crop;selected[0]=id==701?0:1;editor.crop=drafts[selected[0]];editor.invalidate();});
  String key="menglong-v3-crop-preview-"+System.identityHashCode(video);final Surface[] surface={null};
  video.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){public void onSurfaceTextureAvailable(SurfaceTexture t,int w,int h){surface[0]=new Surface(t);UiDiagnostics.guard(a,"V3 裁剪预览绑定",()->MapMirrors.INSTANCE.set(key,surface[0]));}public boolean onSurfaceTextureDestroyed(SurfaceTexture t){MapMirrors.INSTANCE.set(key,null);if(surface[0]!=null){surface[0].release();surface[0]=null;}return true;}public void onSurfaceTextureSizeChanged(SurfaceTexture t,int w,int h){}public void onSurfaceTextureUpdated(SurfaceTexture t){}});
  LinearLayout buttons=new LinearLayout(a);Button full=new Button(a),center=new Button(a);full.setText("完整地图");center.setText("中心区域");buttons.addView(full);buttons.addView(center);body.addView(buttons);full.setOnClickListener(v->{editor.crop=new HudCrop(0,0,1,1);editor.invalidate();});center.setOnClickListener(v->{editor.crop=new HudCrop(.13f,.13f,.87f,.87f);editor.invalidate();});
  if(!MapMirrors.INSTANCE.getStreamActive())hint.append("\n地图流未启动，预览可能为空。请先连接 CarPlay 并开启 HUD 地图。");
  AlertDialog d=new AlertDialog.Builder(a).setTitle("地图来源 · 导航 / 巡航").setView(body).setPositiveButton("保存两套并启用",(dialog,w)->{drafts[selected[0]]=editor.crop;SharedPreferences.Editor p=prefs(a).edit().putBoolean("enabled",true);write(p,true,drafts[0]);write(p,false,drafts[1]);p.apply();refresh(a);}).setNeutralButton("恢复中心范围",(dialog,w)->{prefs(a).edit().putBoolean("enabled",false).apply();refresh(a);}).setNegativeButton("取消",null).create();
  android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());Runnable status=new Runnable(){public void run(){if(!d.isShowing())return;state.setText("当前识别："+(navigating()?"导航":"巡航 / 无路线指引")+"；正在编辑："+(selected[0]==0?"导航位置":"巡航位置"));handler.postDelayed(this,1000);}};
  d.setOnDismissListener(dialog->{handler.removeCallbacksAndMessages(null);MapMirrors.INSTANCE.set(key,null);if(surface[0]!=null){surface[0].release();surface[0]=null;}});d.show();d.getWindow().setLayout(-1,-2);handler.post(status);
 }
 private static void refresh(Activity a){HudOutput.clear();HudOutput.resumed(a);InstrumentOutput.resumed(a);}

 private static final class Editor extends View {
  HudCrop crop;final Paint paint=new Paint(3);float x,y;boolean left,top,right,bottom,drag;
  Editor(Context c,HudCrop crop){super(c);this.crop=crop;setBackgroundColor(Color.TRANSPARENT);}
  protected void onDraw(Canvas canvas){float w=getWidth(),h=getHeight();RectF r=new RectF(crop.getLeft()*w,crop.getTop()*h,crop.getRight()*w,crop.getBottom()*h);paint.setColor(0x77000000);paint.setStyle(Paint.Style.FILL);canvas.drawRect(0,0,w,r.top,paint);canvas.drawRect(0,r.bottom,w,h,paint);canvas.drawRect(0,r.top,r.left,r.bottom,paint);canvas.drawRect(r.right,r.top,w,r.bottom,paint);paint.setColor(0xff47bbff);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3);canvas.drawRect(r,paint);canvas.drawLine(r.centerX()-12,r.centerY(),r.centerX()+12,r.centerY(),paint);canvas.drawLine(r.centerX(),r.centerY()-12,r.centerX(),r.centerY()+12,paint);paint.setStyle(Paint.Style.FILL);for(float px:new float[]{r.left,r.right})for(float py:new float[]{r.top,r.bottom})canvas.drawCircle(px,py,8,paint);}
  public boolean onTouchEvent(android.view.MotionEvent e){if(getWidth()<1||getHeight()<1)return false;float nx=e.getX()/getWidth(),ny=e.getY()/getHeight();if(e.getActionMasked()==0){float tx=28*getResources().getDisplayMetrics().density/getWidth(),ty=28*getResources().getDisplayMetrics().density/getHeight();left=Math.abs(nx-crop.getLeft())<tx;right=!left&&Math.abs(nx-crop.getRight())<tx;top=Math.abs(ny-crop.getTop())<ty;bottom=!top&&Math.abs(ny-crop.getBottom())<ty;drag=nx>=crop.getLeft()-tx&&nx<=crop.getRight()+tx&&ny>=crop.getTop()-ty&&ny<=crop.getBottom()+ty;if(!drag)return false;x=nx;y=ny;getParent().requestDisallowInterceptTouchEvent(true);return true;}if(e.getActionMasked()==2&&drag){float dx=nx-x,dy=ny-y;crop=left||top||right||bottom?crop.resize(dx,dy,left,top,right,bottom):crop.move(dx,dy);x=nx;y=ny;invalidate();return true;}if(e.getActionMasked()==1||e.getActionMasked()==3){boolean was=drag;drag=false;return was;}return drag;}
 }
}
