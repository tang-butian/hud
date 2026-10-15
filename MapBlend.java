package com.shilapi.xcertplay.menglong;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/** Optional polygon/feather compositing of the existing hardware TextureView. */
public final class MapBlend {
 private static SharedPreferences prefs(Context c){return c.getSharedPreferences("menglong_map_blend_213",0);}
 private static String key(int display){return "d"+display+"_";}
 private static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
 private static TextView text(Context c,String s){TextView t=new TextView(c);t.setText(s);t.setTextSize(14);return t;}
 private static Button button(Context c,String s){Button b=new Button(c);b.setText(s);b.setAllCaps(false);return b;}
 public static final class Config {
  boolean enabled,lightKey;int shape=0,feather=12,opacity=100,color=Color.WHITE,revision,lightThreshold=248,lightTransition=32,topFade=0;float[] points=MapShape.preset(3);
  Config copy(){Config c=new Config();c.enabled=enabled;c.lightKey=lightKey;c.lightThreshold=lightThreshold;c.lightTransition=lightTransition;c.topFade=topFade;c.shape=shape;c.feather=feather;c.opacity=opacity;c.color=color;c.revision=revision;c.points=points.clone();return c;}
 }
 private static Config read(Context c,int display){
  SharedPreferences p=prefs(c);String k=key(display);Config s=new Config();s.enabled=p.getBoolean(k+"enabled",false);s.shape=Math.max(0,Math.min(3,p.getInt(k+"shape",0)));s.feather=Math.max(0,Math.min(35,p.getInt(k+"feather",12)));s.opacity=Math.max(30,Math.min(100,p.getInt(k+"opacity",100)));s.color=p.getInt(k+"color",Color.WHITE);s.revision=p.getInt(k+"revision",0);
  s.lightKey=p.getBoolean(k+"light_key",false);s.lightThreshold=Math.max(224,Math.min(255,p.getInt(k+"light_threshold",248)));s.lightTransition=Math.max(8,Math.min(80,p.getInt(k+"light_transition",32)));s.topFade=Math.max(0,Math.min(40,p.getInt(k+"top_fade",0)));
  float[] fallback=MapShape.preset(s.shape);for(int i=0;i<16;i++)s.points[i]=MapShape.clamp(p.getFloat(k+"point"+i,fallback[i]));return s;
 }
 private static void save(Context c,int display,Config s){
  SharedPreferences p=prefs(c);String k=key(display);SharedPreferences.Editor e=p.edit().putBoolean(k+"enabled",s.enabled).putInt(k+"shape",s.shape).putInt(k+"feather",s.feather).putInt(k+"opacity",s.opacity).putInt(k+"color",s.color).putInt(k+"revision",p.getInt(k+"revision",0)+1);
  e.putBoolean(k+"light_key",s.lightKey).putInt(k+"light_threshold",s.lightThreshold).putInt(k+"light_transition",s.lightTransition).putInt(k+"top_fade",s.topFade);for(int i=0;i<16;i++)e.putFloat(k+"point"+i,s.points[i]);e.apply();
 }
 public static void applyReference(Context c,int display){Config s=read(c,display);s.enabled=true;s.shape=0;s.feather=18;s.opacity=100;s.color=Color.WHITE;s.lightKey=true;s.lightThreshold=248;s.lightTransition=32;s.topFade=25;s.points=MapShape.preset(3);save(c,display,s);}
 public interface Test {void show(Config config);}
 public static void settings(Activity a,int display,Runnable refresh,Test test){
  Config config=read(a,display);if(!prefs(a).contains(key(display)+"enabled"))config.enabled=true;
  LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);int pad=dp(a,12);body.setPadding(pad,pad,pad,pad);
  CheckBox enabled=new CheckBox(a);enabled.setText("启用不规则地图与边缘融合");enabled.setChecked(config.enabled);body.addView(enabled);
  body.addView(text(a,"形状只裁剪地图；地图位置大小仍在仪表布局里设置。自定义时拖动八个控制点。"));
  Spinner shapes=new Spinner(a);shapes.setAdapter(new ArrayAdapter<String>(a,android.R.layout.simple_spinner_dropdown_item,new String[]{"圆角区域","椭圆区域","梯形区域","自定义八点轮廓"}));body.addView(shapes);
  ShapeEditor editor=new ShapeEditor(a,config);body.addView(editor,new LinearLayout.LayoutParams(-1,dp(a,220)));
  shapes.setSelection(config.shape);shapes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int index,long id){if(config.shape!=index){config.shape=index;if(index==2)config.points=MapShape.preset(2);else if(index==3)config.points=MapShape.preset(3);editor.invalidate();}}public void onNothingSelected(AdapterView<?> p){}});
  addBar(a,body,"边缘柔化",35,config.feather,n->{config.feather=n;editor.invalidate();});
  addBar(a,body,"整块地图不透明度（100 为完全显示）",70,config.opacity-30,30,n->{config.opacity=n+30;editor.invalidate();});
  CheckBox light=new CheckBox(a);light.setText("日间浅色地图底透明融合");light.setChecked(config.lightKey);body.addView(light);light.setOnCheckedChangeListener((b,on)->config.lightKey=on);
  body.addView(text(a,"减少浅灰和白底，深色道路与彩色路线通常保留。地图里的白色文字、车辆或建筑也可能变淡；夜间地图建议关闭。这里不改变手机高德的地图样式。"));
  addBar(a,body,"浅色透明阈值（低一些会去除更多浅色）",31,config.lightThreshold-224,224,n->config.lightThreshold=n+224);
  addBar(a,body,"浅色过渡宽度",72,config.lightTransition-8,8,n->config.lightTransition=n+8);
  addBar(a,body,"顶部淡出范围（百分比）",40,config.topFade,n->{config.topFade=n;editor.invalidate();});
  body.addView(text(a,"白底颜色微调：降低某个通道会给整张地图着色，不能单独识别背景。配合边缘柔化减轻边界。"));
  String[] channels={"白底红色通道","白底绿色通道","白底蓝色通道"};
  for(int i=0;i<3;i++){final int shift=16-i*8;addBar(a,body,channels[i],79,Math.max(0,((config.color>>shift)&255)-176),176,n->{int value=n+176,mask=255<<shift;config.color=(config.color&~mask)|(value<<shift)|0xff000000;editor.invalidate();});}
  Button reset=button(a,"恢复原来的矩形显示"),blue=button(a,"蓝色区域测试 · 15 秒");body.addView(blue);body.addView(reset);
  blue.setOnClickListener(v->{config.enabled=enabled.isChecked();test.show(config.copy());});
  reset.setOnClickListener(v->{config.enabled=false;save(a,display,config);refresh.run();enabled.setChecked(false);Toast.makeText(a,"已恢复原来的矩形地图",Toast.LENGTH_SHORT).show();});
  body.addView(text(a,"这里不会自动读取原仪表画面或识别速度表位置。蓝色测试可人工校准可见轮廓。无边界效果仍受地图实际底色和原仪表主题影响。"));
  ScrollView scroll=new ScrollView(a);scroll.addView(body);AlertDialog dialog=new AlertDialog.Builder(a).setTitle("地图形状与融合 · 显示器 "+display).setView(scroll).setPositiveButton("保存并应用",(d,w)->{config.enabled=enabled.isChecked();save(a,display,config);refresh.run();}).setNegativeButton("取消",null).create();dialog.show();dialog.getWindow().setLayout(-1,-1);
 }
 private interface Value {void set(int n);}
 private static void addBar(Context c,LinearLayout parent,String label,int max,int initial,Value changed){
  addBar(c,parent,label,max,initial,0,changed);
 }
 private static void addBar(Context c,LinearLayout parent,String label,int max,int initial,int offset,Value changed){
  TextView t=text(c,label+"："+(initial+offset));parent.addView(t);SeekBar bar=new SeekBar(c);bar.setMax(max);bar.setProgress(Math.max(0,Math.min(max,initial)));parent.addView(bar);bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){t.setText(label+"："+(n+offset));changed.set(n);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
 }
 private static Bitmap mask(Config c,int width,int height){
  return mask(c,width,height,new RectF(0,0,width,height));
 }
 private static Bitmap mask(Config c,int width,int height,RectF video){
  float scale=Math.min(1,512f/Math.max(width,height));int w=Math.max(1,Math.round(width*scale)),h=Math.max(1,Math.round(height*scale));int[] pixels=new int[w*h];
  float left=video.left*w/width,top=video.top*h/height,right=video.right*w/width,bottom=video.bottom*h/height;int vw=Math.max(1,Math.round(right-left)),vh=Math.max(1,Math.round(bottom-top));float feather=Math.min(vw,vh)*c.feather/100f;
  for(int y=0;y<h;y++)for(int x=0;x<w;x++){float px=x+.5f,py=y+.5f;if(px>=left&&px<right&&py>=top&&py<bottom)pixels[y*w+x]=(Math.round(MapShape.alpha(MapShape.distance(c.shape,c.points,px-left,py-top,vw,vh),feather)*MapTone.topFade(py-top,bottom-top,c.topFade))<<24)|0xffffff;}
  Bitmap image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);image.setPixels(pixels,0,w,0,0,w,h);return image;
 }
 public static final class Layer extends FrameLayout {
  private final int display;private Config current,test;private Bitmap alpha,redAlpha;private final Paint maskPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG),layerPaint=new Paint(Paint.ANTI_ALIAS_FLAG),tonePaint=new Paint(Paint.ANTI_ALIAS_FLAG),redPaint=new Paint(Paint.ANTI_ALIAS_FLAG),greenPaint=new Paint(Paint.ANTI_ALIAS_FLAG),bluePaint=new Paint(Paint.ANTI_ALIAS_FLAG);private boolean lastNight;private long testEnd;private int width,height,maskRevision=-1,toneRevision=-1;private final RectF lastVideo=new RectF();private final Handler handler=new Handler(Looper.getMainLooper());
  public Layer(Context c,int display){super(c);this.display=display;setBackgroundColor(Color.TRANSPARENT);setClipChildren(true);maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));current=read(c,display);redPaint.setColorFilter(new ColorMatrixColorFilter(MapTone.redAccent()));greenPaint.setColorFilter(new ColorMatrixColorFilter(NightTone.chroma(1)));bluePaint.setColorFilter(new ColorMatrixColorFilter(NightTone.chroma(2)));}
  public void refresh(){Config next=read(getContext(),display);if(next.revision!=current.revision){current=next;alpha=null;toneRevision=-1;invalidate();}}
  public void test(Config config){test=config;testEnd=SystemClock.uptimeMillis()+15000;alpha=null;invalidate();handler.removeCallbacksAndMessages(null);handler.postDelayed(()->{test=null;alpha=null;invalidate();},15000);}
  @Override protected void onDetachedFromWindow(){handler.removeCallbacksAndMessages(null);test=null;alpha=null;redAlpha=null;super.onDetachedFromWindow();}
  @Override protected void dispatchDraw(Canvas canvas){
   if(!UiDiagnostics.guard(getContext(),"地图轮廓与边缘合成",()->drawComposite(canvas))){current.enabled=false;test=null;alpha=null;prefs(getContext()).edit().putBoolean(key(display)+"enabled",false).apply();super.dispatchDraw(canvas);}
  }
  private void drawComposite(Canvas canvas){
   Config c=test!=null&&SystemClock.uptimeMillis()<testEnd?test:current;boolean calibration=c==test;boolean night=!calibration&&c.lightKey&&NightTheme.isNight();if(night!=lastNight){lastNight=night;alpha=null;toneRevision=-1;}
   if(!c.enabled){super.dispatchDraw(canvas);if(calibration)canvas.drawColor(0xff1389dc);return;}
   int w=getWidth(),h=getHeight();if(w<1||h<1)return;
   RectF video=new RectF(0,0,w,h);if(getChildCount()>0){View child=getChildAt(0);if(child.getWidth()>0&&child.getHeight()>0)video.set(Math.max(0,child.getLeft()),Math.max(0,child.getTop()),Math.min(w,child.getRight()),Math.min(h,child.getBottom()));}
   if(alpha==null||width!=w||height!=h||maskRevision!=c.revision||!lastVideo.equals(video)){Config effective=c.copy();if(night){effective.topFade=Math.max(35,c.topFade);effective.feather=Math.max(20,c.feather);}alpha=mask(effective,w,h,video);Config redConfig=c.copy();redConfig.topFade=0;redAlpha=mask(redConfig,w,h,video);width=w;height=h;maskRevision=c.revision;lastVideo.set(video);}
   layerPaint.setAlpha(calibration?210:Math.round(c.opacity*2.55f));
   layerPaint.setColorFilter(null);
   if(!calibration&&toneRevision!=c.revision){tonePaint.setColorFilter(new ColorMatrixColorFilter(night?new float[]{1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0}:MapTone.matrix(c.color,c.lightKey,c.lightThreshold,c.lightTransition)));toneRevision=c.revision;}
   int save=canvas.saveLayer(0,0,w,h,layerPaint);try{
    if(!calibration)drawTextureLayer(canvas,w,h,tonePaint);
    else{super.dispatchDraw(canvas);canvas.drawColor(0xff1389dc);}
    canvas.drawBitmap(alpha,null,new RectF(0,0,w,h),maskPaint);
    // Draw the same decoded GPU texture, never read the screen or infer overspeed.
    // Apply the red gate before the geometry mask; keep red within the chosen map boundary.
    if(!calibration&&!night&&c.lightKey&&redAlpha!=null)drawAccent(canvas,w,h,redPaint,redAlpha);

   }finally{canvas.restoreToCount(save);}
  }
  private void drawTextureLayer(Canvas canvas,int w,int h,Paint filter){int saved=canvas.saveLayer(0,0,w,h,filter);try{super.dispatchDraw(canvas);}finally{canvas.restoreToCount(saved);}}
  private void drawAccent(Canvas canvas,int w,int h,Paint color,Bitmap geometry){int saved=canvas.saveLayer(0,0,w,h,null);try{drawTextureLayer(canvas,w,h,color);canvas.drawBitmap(geometry,null,new RectF(0,0,w,h),maskPaint);}finally{canvas.restoreToCount(saved);}}
 }
 private static final class ShapeEditor extends View {
  private final Config config;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);private final RectF bounds=new RectF();private int selected=-1;
  ShapeEditor(Context c,Config config){super(c);this.config=config;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);int pad=dp(getContext(),18);bounds.set(pad,pad,getWidth()-pad,getHeight()-pad);canvas.drawColor(0xfff3f0e9);if(bounds.width()<1||bounds.height()<1)return;
   int w=Math.max(1,Math.round(bounds.width())),h=Math.max(1,Math.round(bounds.height()));Bitmap mask=mask(config,w,h);paint.setColorFilter(new PorterDuffColorFilter(0xff77b8d6,PorterDuff.Mode.SRC_IN));paint.setAlpha(Math.round(config.opacity*2.55f));canvas.drawBitmap(mask,null,bounds,paint);paint.setColorFilter(null);paint.setAlpha(255);
   if(config.shape>=2){paint.setColor(0xff086692);for(int i=0;i<8;i++){float x=bounds.left+config.points[i*2]*bounds.width(),y=bounds.top+config.points[i*2+1]*bounds.height();canvas.drawCircle(x,y,dp(getContext(),7),paint);}}
  }
  @Override public boolean onTouchEvent(MotionEvent event){if(config.shape!=3)return false;float x=event.getX(),y=event.getY();if(event.getActionMasked()==MotionEvent.ACTION_DOWN){selected=-1;float limit=dp(getContext(),28);for(int i=0;i<8;i++){float distance=(float)Math.hypot(x-bounds.left-config.points[2*i]*bounds.width(),y-bounds.top-config.points[2*i+1]*bounds.height());if(distance<limit){limit=distance;selected=i;}}if(selected<0)return false;getParent().requestDisallowInterceptTouchEvent(true);return true;}
   if(selected>=0&&event.getActionMasked()==MotionEvent.ACTION_MOVE){config.points[2*selected]=MapShape.clamp((x-bounds.left)/bounds.width());config.points[2*selected+1]=MapShape.clamp((y-bounds.top)/bounds.height());invalidate();return true;}
   if(event.getActionMasked()==MotionEvent.ACTION_UP||event.getActionMasked()==MotionEvent.ACTION_CANCEL){boolean handled=selected>=0;selected=-1;getParent().requestDisallowInterceptTouchEvent(false);if(handled)performClick();return handled;}return selected>=0;
  }
  @Override public boolean performClick(){super.performClick();return true;}
 }
}
