package com.shilapi.xcertplay.menglong;



import android.app.*;

import android.content.*;

import android.graphics.*;

import android.hardware.display.DisplayManager;

import android.media.MediaMetadata;

import android.media.session.MediaSession;

import android.os.*;

import android.text.TextUtils;

import android.util.DisplayMetrics;

import android.view.*;

import android.widget.*;

import com.shilapi.xcertplay.CarPlayMediaKeys;

import com.shilapi.xcertplay.glance.CarPlayGlance;

import com.shilapi.xcertplay.hud.BydManeuverCodes;

import com.shilapi.xcertplay.menglong.LayoutGeometry.Box;

import java.io.*;

import java.lang.ref.WeakReference;

import java.util.*;



/** Independent metadata HUD; no video capture. */

public final class HudOutput {

 private static final int MINT=0xff72ffd1;

 private static Presentation window;private static HudRoot mainView;private static WeakReference<Activity> owner=new WeakReference<Activity>(null);

 private static SharedPreferences prefs(Context c){return c.getSharedPreferences("menglong_hud_214",0);}

 /** Request the shared map stream without enabling any ordinary instrument window. */

 public static boolean mapRequested(Context c,boolean original){SharedPreferences p=prefs(c);return original||(p.getBoolean("enabled",false)&&p.getBoolean("map",false));}

 private static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}

 private static Box mapDefault(){return new Box(.04f,.04f,.96f,.96f);}

 private static Box navDefault(){return new Box(.05f,.06f,.63f,.53f);}

 private static Box songDefault(){return new Box(.06f,.60f,.94f,.94f);}

 private static Box read(SharedPreferences p,String k,Box d){return LayoutGeometry.valid(new Box(p.getFloat(k+"l",d.l),p.getFloat(k+"t",d.t),p.getFloat(k+"r",d.r),p.getFloat(k+"b",d.b)));}

 private static void write(SharedPreferences.Editor e,String k,Box b){e.putFloat(k+"l",b.l).putFloat(k+"t",b.t).putFloat(k+"r",b.r).putFloat(k+"b",b.b);}

 private static String key(Display d){return "display"+d.getDisplayId()+"_";}

 public static void created(Activity a){SharedPreferences detailPrefs=prefs(a);if(!detailPrefs.getBoolean("detail85_seeded",false)){detailPrefs.edit().putBoolean("detail85_seeded",true).putInt("before85_brightness",detailPrefs.getInt("map_brightness",160)).putInt("before85_contrast",detailPrefs.getInt("map_contrast",120)).putInt("before85_sharpness",detailPrefs.getInt("map_sharpness",35)).putBoolean("before85_bold",detailPrefs.getBoolean("map_bold",true)).putInt("map_brightness",100).putInt("map_contrast",100).putInt("map_sharpness",0).putBoolean("map_bold",false).apply();}MicrophoneCompatibility.created(a);UiDiagnostics.install(a);SharedPreferences hudPrefs=prefs(a);if(!hudPrefs.getBoolean("daylight74_seeded",false)){SharedPreferences.Editor he=hudPrefs.edit().putBoolean("daylight74_seeded",true);if(!hudPrefs.contains("map_brightness"))he.putInt("map_brightness",160);if(!hudPrefs.contains("map_contrast"))he.putInt("map_contrast",120);if(!hudPrefs.contains("map_sharpness"))he.putInt("map_sharpness",35);if(!hudPrefs.contains("map_bold"))he.putBoolean("map_bold",true);he.apply();}if(!hudPrefs.getBoolean("transparent64_seeded",false))hudPrefs.edit().putBoolean("map",true).putInt("opacity",0).putBoolean("transparent64_seeded",true).apply();SharedPreferences audio=a.getSharedPreferences("menglong_wireless_audio_214",0);if(!audio.getBoolean("baseline215_seeded",false))audio.edit().putBoolean("compatibility",false).putBoolean("baseline215_seeded",true).commit();

  SharedPreferences p=prefs(a);if(p.getBoolean("haval_icon_seeded",false))return;

  Bitmap b=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(0xffbf2231);c.drawRoundRect(new RectF(0,0,256,256),48,48,paint);paint.setColor(Color.WHITE);paint.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(62);c.drawText("HAVAL",128,135,paint);paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(26);c.drawText("哈弗汽车",128,184,paint);

  File temp=new File(a.getFilesDir(),"airplay-icon-haval.tmp"),dest=new File(a.getFilesDir(),"airplay-icon.png");

  boolean compressed=false;try(FileOutputStream out=new FileOutputStream(temp)){compressed=b.compress(Bitmap.CompressFormat.PNG,100,out);out.flush();}

  catch(IOException e){temp.delete();return;}finally{b.recycle();}if(compressed&&temp.renameTo(dest))p.edit().putBoolean("haval_icon_seeded",true).apply();else temp.delete();

 }

 public static void resumed(Activity a){

  if(a.isFinishing()||a.isDestroyed())return;

  restore(a);

 }

 public static void destroyed(Activity a){if(owner.get()==a)close();}

 private static void close(){Presentation old=window;HudRoot view=mainView;window=null;mainView=null;owner.clear();if(old!=null)old.dismiss();if(view!=null){view.stop();if(view.getParent() instanceof ViewGroup)((ViewGroup)view.getParent()).removeView(view);}}

 public static void clear(){close();}
 public static boolean externalTarget(Context c,int display){SharedPreferences p=prefs(c);return display!=0&&p.getBoolean("enabled",false)&&p.getBoolean("map",false)&&p.getInt("display",-1)==display;}


 private static void restore(Activity a){if(!ProjectionGate.visible(a)){close();return;}SharedPreferences p=prefs(a);if(!p.getBoolean("enabled",false)){close();return;}Display d=((DisplayManager)a.getSystemService(Context.DISPLAY_SERVICE)).getDisplay(p.getInt("display",-1));if(d==null){close();return;}

  if(owner.get()==a&&((window!=null&&window.isShowing()&&window.getDisplay().getDisplayId()==d.getDisplayId())||(mainView!=null&&d.getDisplayId()==0)))return;close();owner=new WeakReference<Activity>(a);

  try{if(d.getDisplayId()==0){mainView=new HudRoot(a,a,d);mainView.setClickable(false);((ViewGroup)a.getWindow().getDecorView()).addView(mainView,new FrameLayout.LayoutParams(-1,-1));mainView.start();}

   else{if(p.getBoolean("map",false)){HudRoot overlay=new HudRoot(a,a,d,false);window=InstrumentOutput.hudWindow(a,d,read(p,key(d)+"box2_",mapDefault()),p.getInt("focus_scale_v5",135),p.getBoolean("mirror",false),overlay,()->overlay.start(),()->overlay.stop());}else window=new HudWindow(a,d);window.show();window.getWindow().setLayout(-1,-1);window.getWindow().setGravity(Gravity.TOP|Gravity.LEFT);window.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);}}

  catch(RuntimeException e){close();Toast.makeText(a,"无法打开 HUD 显示器："+e.getClass().getSimpleName(),Toast.LENGTH_LONG).show();}

 }

 private static Spinner spinner(Context c,String[] values){Spinner s=new Spinner(c);s.setAdapter(new ArrayAdapter<String>(c,android.R.layout.simple_spinner_dropdown_item,values));return s;}

 private static Button button(Context c,String s){Button b=new Button(c);b.setText(s);b.setTextSize(13);b.setAllCaps(false);return b;}

 private static CheckBox check(Context c,String label,boolean on){CheckBox b=new CheckBox(c);b.setText(label);b.setTextSize(13);b.setChecked(on);return b;}

 private static TextView label(Context c,String s){TextView t=new TextView(c);t.setText(s);t.setTextSize(13);return t;}

 public static void settings(Activity a){

  SharedPreferences p=prefs(a);List<Display> list=Arrays.asList(((DisplayManager)a.getSystemService(Context.DISPLAY_SERVICE)).getDisplays());if(list.isEmpty())return;

  String[] names=new String[list.size()];int selected=0;for(int i=0;i<list.size();i++){Display d=list.get(i);DisplayMetrics m=new DisplayMetrics();d.getRealMetrics(m);names[i]=(d.getDisplayId()==0?"中控 · DiPlay 界面内":"显示器 "+d.getDisplayId()+" · "+d.getName())+" · "+m.widthPixels+"×"+m.heightPixels;if(d.getDisplayId()==p.getInt("display",3))selected=i;}

  LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(a,14),dp(a,6),dp(a,14),dp(a,6));body.addView(label(a,"HUD · 选择实际可见的显示器，地图、导航提示与曲名 / 歌词各自拖动缩放"));String raw=CarPlayGlance.INSTANCE.menglongSongLine();MediaSession current=CarPlayMediaKeys.menglongSession();MediaMetadata metadata=current==null?null:current.getController().getMetadata();MusicText.Rows received=MusicText.separate(metadata==null?null:metadata.getString(MediaMetadata.METADATA_KEY_TITLE),metadata==null?null:metadata.getString(MediaMetadata.METADATA_KEY_ARTIST),raw);body.addView(label(a,"地图流："+(com.shilapi.xcertplay.MapMirrors.INSTANCE.getStreamActive()?"已启动":"未启动，首次启用后需重新连接")+" · 歌词："+(!received.lyrics.isEmpty()?"已收到":raw==null||raw.trim().isEmpty()?"未收到":"只有曲名 / 歌手，暂无歌词")));body.addView(label(a,"HUD地图："+HudMap.status+" · 附加显示器复用仪表地图输出窗口"));Spinner displays=spinner(a,names);body.addView(displays);

  LayoutEditor editor=new LayoutEditor(a,p);LinearLayout row=new LinearLayout(a);String[] titles={"导航提示","曲名 / 歌词","地图区域"};Button[] selectButtons=new Button[3];TextView selectedLabel=label(a,"");

  Runnable mark=()->{for(int n=0;n<3;n++){boolean active=editor.selection.selected()==n;selectButtons[n].setText(active?"✓ "+titles[n]:titles[n]);selectButtons[n].setTextColor(active?0xff117049:Color.DKGRAY);}selectedLabel.setText("当前调节："+titles[editor.selection.selected()]+" · 其他区域保持不动");editor.invalidate();};

  for(int i=0;i<3;i++){final int index=i;Button b=button(a,titles[i]);selectButtons[i]=b;row.addView(b,new LinearLayout.LayoutParams(0,-2,1));b.setOnClickListener(v->{editor.selection.select(index);mark.run();});}body.addView(row);body.addView(selectedLabel);mark.run();body.addView(editor,new LinearLayout.LayoutParams(-1,dp(a,210)));

  CheckBox moveOnly=check(a,"移动模式（拖框内部移动；取消勾选后可拉边框缩放）",true);body.addView(moveOnly);moveOnly.setOnCheckedChangeListener((b,on)->editor.moveOnly=on);
  LinearLayout nudges=new LinearLayout(a);String[] steps={"←","↑","↓","→"};int[] dx={-1,0,0,1},dy={0,-1,1,0};for(int i=0;i<4;i++){final int n=i;Button step=button(a,steps[i]);step.setOnClickListener(v->{editor.nudge(dx[n],dy[n]);mark.run();});nudges.addView(step,new LinearLayout.LayoutParams(0,-2,1));}body.addView(nudges);body.addView(label(a,"先选地图区域再拖动；箭头每次微调 2 个显示像素。保存后更新实际 HUD。"));
  Button fitMap=button(a,"地图适配当前 HUD 屏幕");fitMap.setOnClickListener(v->{editor.selection.select(2);editor.boxes[2]=new Box(.04f,.04f,.96f,.96f);mark.run();});body.addView(fitMap);

  LinearLayout options=new LinearLayout(a);CheckBox nav=check(a,"导航提示",p.getBoolean("nav",true)),song=check(a,"曲名 / 歌词",p.getBoolean("song",true)),map=check(a,"精简地图",p.getBoolean("map",false)),mirror=check(a,"镜像",p.getBoolean("mirror",false));options.addView(nav);options.addView(song);options.addView(map);options.addView(mirror);body.addView(options);

  LinearLayout controls=new LinearLayout(a);controls.setGravity(Gravity.CENTER_VERTICAL);TextView fontLabel=label(a,"字号");controls.addView(fontLabel);SeekBar font=new SeekBar(a);font.setMax(40);font.setProgress(Math.max(0,Math.min(40,p.getInt("font",28)-6)));controls.addView(font,new LinearLayout.LayoutParams(0,dp(a,32),1));body.addView(controls);fontLabel.setText("字号 "+(font.getProgress()+6));font.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int n,boolean user){fontLabel.setText("字号 "+(n+6));}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});

  Button colors=button(a,"HUD 歌词颜色");colors.setOnClickListener(v->MusicStyle.hudSettings(a));body.addView(colors);

  Button v3Crop=button(a,"地图来源（拖动 / 缩放裁剪）");v3Crop.setOnClickListener(v->V3HudCrop.settings(a));body.addView(v3Crop);
  TextView mapLabel=label(a,"");SeekBar mapScale=new SeekBar(a);mapScale.setMax(100);mapScale.setProgress(Math.max(0,Math.min(100,p.getInt("focus_scale_v5",135)-80)));mapLabel.setText("中心地图范围 "+(mapScale.getProgress()+80)+"% · 数值越大，显示的周边范围越少");body.addView(mapLabel);body.addView(mapScale,new LinearLayout.LayoutParams(-1,dp(a,28)));mapScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){mapLabel.setText("中心地图范围 "+(n+80)+"%");}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});

  body.addView(label(a,"HUD 独立地图模式：保留高德原画面颜色，仅做来源裁剪、位置缩放和 V3 边缘遮罩。仪表处理独立。"));Button edge=button(a,"HUD 边缘遮罩");edge.setOnClickListener(v->V3HudFrame.settings(a));body.addView(edge);

  ScrollView scroll=new ScrollView(a);scroll.setFillViewport(true);scroll.addView(body,new ScrollView.LayoutParams(-1,-2));AlertDialog dialog=new AlertDialog.Builder(a).setView(scroll).setPositiveButton("保存并显示",null).setNeutralButton("关闭 HUD",null).setNegativeButton("取消",null).create();

  displays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> parent,View view,int pos,long id){editor.load(list.get(pos));}public void onNothingSelected(AdapterView<?> parent){}});displays.setSelection(selected);editor.load(list.get(selected));

  dialog.setOnShowListener(d->{dialog.getWindow().setLayout(-1,-1);dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{p.edit().putBoolean("enabled",false).apply();close();InstrumentOutput.resumed(a);dialog.dismiss();});dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(!nav.isChecked()&&!song.isChecked()&&!map.isChecked()){Toast.makeText(a,"请至少选择一项 HUD 内容",Toast.LENGTH_SHORT).show();return;}boolean needsReconnect=map.isChecked()&&!com.shilapi.xcertplay.MapMirrors.INSTANCE.getStreamActive();if(map.isChecked())a.getSharedPreferences("xcertplay_airplay",0).edit().putBoolean("cluster_map_enabled",true).putString("cluster_content","MAP").apply();Display target=list.get(displays.getSelectedItemPosition());SharedPreferences.Editor e=p.edit().putBoolean("enabled",true).putInt("display",target.getDisplayId()).putBoolean("nav",nav.isChecked()).putBoolean("song",song.isChecked()).putBoolean("map",map.isChecked()).putInt("focus_scale_v5",mapScale.getProgress()+80).putBoolean("light",false).putBoolean("mirror",mirror.isChecked()).putInt("font",font.getProgress()+6).putInt("opacity",0);String key=key(target);for(int i=0;i<3;i++)write(e,key+"box"+i+"_",editor.boxes[i]);e.apply();close();restore(a);InstrumentOutput.resumed(a);dialog.dismiss();if(needsReconnect)Toast.makeText(a,"HUD 地图已启用，请断开并重新连接 CarPlay 以启动独立地图流",Toast.LENGTH_LONG).show();});});dialog.show();

 }

 private static final class LayoutEditor extends View {

  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final RectF screen=new RectF();private final SharedPreferences p;Box[] boxes={navDefault(),songDefault(),mapDefault()};final EditSelection selection=new EditSelection(3);int displayW=1280,displayH=480,edges;boolean dragging,moveOnly=true;Box start;float downX,downY;

  LayoutEditor(Context c,SharedPreferences p){super(c);this.p=p;selection.select(2);}
  void nudge(int dx,int dy){int n=selection.selected();boxes[n]=LayoutGeometry.move(boxes[n],dx*2f/displayW,dy*2f/displayH);invalidate();}

  void load(Display d){DisplayMetrics m=new DisplayMetrics();d.getRealMetrics(m);displayW=Math.max(1,m.widthPixels);displayH=Math.max(1,m.heightPixels);String k=key(d);Box[] defaults={navDefault(),songDefault(),mapDefault()};for(int i=0;i<3;i++)boxes[i]=read(p,k+"box"+i+"_",defaults[i]);invalidate();}

  private RectF rect(Box b){return new RectF(screen.left+b.l*screen.width(),screen.top+b.t*screen.height(),screen.left+b.r*screen.width(),screen.top+b.b*screen.height());}

  @Override protected void onDraw(Canvas c){super.onDraw(c);float pad=dp(getContext(),18),scale=Math.min(Math.max(1,getWidth()-pad*2)/displayW,Math.max(1,getHeight()-pad*2)/displayH),w=displayW*scale,h=displayH*scale;screen.set((getWidth()-w)/2,(getHeight()-h)/2,(getWidth()+w)/2,(getHeight()+h)/2);paint.setStyle(Paint.Style.FILL);paint.setColor(Color.BLACK);c.drawRect(screen,paint);

   String[] labels={"↗  300 米 · 下一道路","曲名与歌词分行显示","独立地图流 · 区域示意"};for(int order=0;order<3;order++){int i=(selection.selected()+1+order)%3;RectF r=rect(boxes[i]);paint.setColor(0xff14251d);c.drawRoundRect(r,8,8,paint);{paint.setTextSize(Math.min(dp(getContext(),16),r.height()*.28f));paint.setColor(MINT);paint.setTextAlign(Paint.Align.CENTER);c.drawText(labels[i],r.centerX(),r.centerY(),paint);}}

   RectF active=rect(boxes[selection.selected()]);paint.setColor(MINT);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(getContext(),2));c.drawRect(active,paint);paint.setStyle(Paint.Style.FILL);for(float x:new float[]{active.left,active.centerX(),active.right})for(float y:new float[]{active.top,active.centerY(),active.bottom})if(x!=active.centerX()||y!=active.centerY())c.drawCircle(x,y,dp(getContext(),5),paint);

  }

  @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();if(screen.width()<1||screen.height()<1)return false;if(e.getActionMasked()==MotionEvent.ACTION_DOWN){float hit=dp(getContext(),20);if(!selection.begin(boxes,(x-screen.left)/screen.width(),(y-screen.top)/screen.height(),hit/screen.width(),hit/screen.height()))return false;RectF active=rect(boxes[selection.target()]);float hx=Math.min(hit,active.width()*.25f),hy=Math.min(hit,active.height()*.25f);edges=0;if(!moveOnly){if(Math.abs(x-active.left)<=hx)edges|=1;else if(Math.abs(x-active.right)<=hx)edges|=4;if(Math.abs(y-active.top)<=hy)edges|=2;else if(Math.abs(y-active.bottom)<=hy)edges|=8;}start=boxes[selection.target()].copy();downX=x;downY=y;dragging=true;getParent().requestDisallowInterceptTouchEvent(true);invalidate();return true;}

   if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&dragging){boxes[selection.target()]=LayoutGeometry.resize(start,edges,(x-downX)/screen.width(),(y-downY)/screen.height());invalidate();return true;}if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){boolean had=dragging;dragging=false;selection.end();getParent().requestDisallowInterceptTouchEvent(false);if(had&&e.getActionMasked()==MotionEvent.ACTION_UP)performClick();return had;}return dragging;

  }

  @Override public boolean performClick(){super.performClick();return true;}

 }



 private static final class HudWindow extends Presentation {

  private final Activity activity;private HudRoot hud;HudWindow(Activity a,Display d){super(a,d);activity=a;}

  @Override protected void onCreate(Bundle b){super.onCreate(b);getWindow().setBackgroundDrawableResource(android.R.color.transparent);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);hud=new HudRoot(getContext(),activity,getDisplay());setContentView(hud);}

  @Override protected void onStart(){super.onStart();hud.start();}

  @Override protected void onStop(){hud.stop();super.onStop();if(window==this){window=null;owner.clear();}}

 }

 private static final class HudRoot extends FrameLayout {

  final HudView text;final HudMap map;

  HudRoot(Context c,Activity a,Display d){this(c,a,d,true);}
  HudRoot(Context c,Activity a,Display d,boolean ownMap){super(c);setBackgroundColor(Color.TRANSPARENT);setClipChildren(true);SharedPreferences p=prefs(c);map=ownMap&&p.getBoolean("map",false)?new HudMap(c,key(d),read(p,key(d)+"box2_",mapDefault()),p.getInt("focus_scale_v5",135),p.getBoolean("mirror",false)):null;if(map!=null)addView(map,new FrameLayout.LayoutParams(-1,-1));text=new HudView(c,a,d);addView(text,new FrameLayout.LayoutParams(-1,-1));}

  void start(){text.start();}void stop(){text.stop();if(map!=null)map.stop();}

 }

 private static final class HudView extends View {

  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);private final Handler handler=new Handler(Looper.getMainLooper());private final Box[] areas=new Box[2];private final boolean nav,song,mirror;private final int font;private boolean running;private String distance="",road="",songText="",songName="",artist="";private boolean connected;private int navCode;

  HudView(Context context,Activity a,Display d){super(context);SharedPreferences p=prefs(context);String k=key(d);areas[0]=read(p,k+"box0_",navDefault());areas[1]=read(p,k+"box1_",songDefault());nav=p.getBoolean("nav",true);song=p.getBoolean("song",true);mirror=p.getBoolean("mirror",false);font=p.getInt("font",28);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}

  void start(){running=true;handler.post(tick);}void stop(){running=false;handler.removeCallbacksAndMessages(null);}

  private final Runnable tick=new Runnable(){public void run(){if(!running)return;if(!UiDiagnostics.guard(getContext(),"HUD 导航 / 歌词更新",()->update())){stop();prefs(getContext()).edit().putBoolean("enabled",false).apply();handler.post(()->UiDiagnostics.guard(getContext(),"关闭异常 HUD",()->close()));return;}invalidate();handler.postDelayed(this,500);}};

  private void update(){NightTheme.refresh(getContext());CarPlayGlance.Snapshot s=CarPlayGlance.INSTANCE.snapshot();connected=s!=null&&s.getConnected();

   if(connected&&s.getManeuverType()!=null){int code=BydManeuverCodes.INSTANCE.gaode(s.getManeuverType(),s.getDrivingSide());navCode=code;distance=HudFormat.distance(s.getDistanceMeters());road=s.getRoad()==null?"":s.getRoad();}

   else{navCode=0;distance="";road="";}

   String text=connected?CarPlayGlance.INSTANCE.menglongSongLine():null;MediaSession session=CarPlayMediaKeys.menglongSession();MediaMetadata meta=!connected||session==null?null:session.getController().getMetadata();String name=meta==null?null:MusicPalette.first(meta.getString(MediaMetadata.METADATA_KEY_TITLE),meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE));artist=meta==null?"":meta.getString(MediaMetadata.METADATA_KEY_ARTIST);if(artist==null)artist="";MusicText.Rows rows=MusicText.separate(name,artist,text);songName=rows.title;artist=rows.artist;songText=rows.lyrics;

  }

  private RectF rect(Box b){return new RectF(b.l*getWidth(),b.t*getHeight(),b.r*getWidth(),b.b*getHeight());}

  private float size(RectF r,int lines){return Math.max(1,Math.min(font*getResources().getDisplayMetrics().scaledDensity,Math.min(r.height()/(lines*1.55f),r.width()/10)));}

  private void line(Canvas c,String s,float x,float y,float px,int color,float width,boolean bold){paint.setColor(color);paint.setTextAlign(Paint.Align.LEFT);paint.setTextSize(px);paint.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);CharSequence clipped=TextUtils.ellipsize(s==null?"":s,new android.text.TextPaint(paint),Math.max(1,width),TextUtils.TruncateAt.END);c.drawText(clipped.toString(),x,y,paint);}

  @Override protected void onDraw(Canvas c){super.onDraw(c);int save=c.save();if(mirror)c.scale(-1,1,getWidth()/2f,getHeight()/2f);

   if(nav&&connected&&navCode!=0){RectF r=rect(areas[0]);int clip=c.save();c.clipRect(r);GaodeCard.drawTransparent(c,r,navCode,distance,road);c.restoreToCount(clip);}

   if(song&&(!songName.isEmpty()||!songText.isEmpty())){RectF r=rect(areas[1]);int clip=c.save();c.clipRect(r);String[] lines=songText.isEmpty()?new String[0]:songText.split("\r?\n",3);int titleRows=songName.isEmpty()?0:1;float px=size(r,Math.max(1,titleRows+lines.length)),x=r.left+r.width()*.05f,y=r.top+px*1.3f,avail=r.width()*.9f;int color=MusicStyle.hudLyric(getContext());if(titleRows>0)line(c,songName,x,y,px,color,avail,true);for(int i=0;i<Math.min(3,lines.length);i++)line(c,lines[i],x,y+px*(titleRows+i)*1.25f,px,color,avail,false);c.restoreToCount(clip);}

   c.restoreToCount(save);

  }

 }

}



