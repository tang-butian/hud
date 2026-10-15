package com.shilapi.xcertplay.menglong;



import android.app.*;

import android.content.*;

import android.graphics.*;

import android.graphics.drawable.GradientDrawable;

import android.hardware.display.DisplayManager;

import android.media.MediaMetadata;

import android.media.session.*;

import android.media.session.MediaController;

import android.os.*;

import android.text.TextUtils;

import android.util.DisplayMetrics;

import android.view.*;

import android.widget.*;

import com.shilapi.xcertplay.MapMirrors;

import com.shilapi.xcertplay.CarPlayMediaKeys;

import com.shilapi.xcertplay.glance.CarPlayGlance;

import java.lang.ref.WeakReference;

import java.util.*;

import com.shilapi.xcertplay.menglong.LayoutGeometry.Box;



public final class InstrumentOutput {

    private static final int BG=0xff080d15, CARD=0xff141e2b, MINT=0xff53e0bc, BLUE=0xff66b9ff, MUTED=0xffa5b6c8;

    private static Output shown;

    private static WeakReference<Activity> owner=new WeakReference<Activity>(null);

    private static SharedPreferences prefs(Context c){return c.getSharedPreferences("menglong_instrument_214",0);}
    private static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}

    private static GradientDrawable panel(int color,float radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}

    public static void resumed(final Activity a){ProjectionGate.resumed(a);a.getWindow().getDecorView().post(()->{

        if(a.isFinishing()||a.isDestroyed())return;

        if(!ProjectionGate.isHost(a)){SettingsHub.resumed(a);UiDiagnostics.resumed(a);AudioCompatibility.resumed(a);return;}

        NightTheme.refresh(a);NavPrompt.foreground(a);UiDiagnostics.guard(a,"仪表默认显示区域",()->upgradeMap(a));

        SettingsHub.resumed(a);UiDiagnostics.resumed(a);if(!UiDiagnostics.guard(a,"恢复仪表",()->restore(a)))prefs(a).edit().putBoolean("enabled",false).apply();UiDiagnostics.guard(a,"恢复 HUD",()->HudOutput.resumed(a));UiDiagnostics.guard(a,"无线音频入口",()->AudioCompatibility.resumed(a));UiDiagnostics.guard(a,"恢复 App 投屏",()->AppOutput.resumed(a));

    });}

    public static void destroyed(Activity a){if(owner.get()==a)close();HudOutput.destroyed(a);AppOutput.destroyed(a);}

    private static void close(){Output old=shown;shown=null;owner.clear();if(old!=null)old.dismiss();}

    public static void clear(){close();}

    private static void restore(Activity a){

        if(!ProjectionGate.visible(a)){close();return;}

        SharedPreferences p=prefs(a);if(!p.getBoolean("enabled",false)){close();return;}

        Display d=((DisplayManager)a.getSystemService(Context.DISPLAY_SERVICE)).getDisplay(p.getInt("display",-1));

        if(d==null||d.getDisplayId()==Display.DEFAULT_DISPLAY||HudOutput.externalTarget(a,d.getDisplayId())){close();return;}

        if(shown!=null&&shown.isShowing()&&owner.get()==a&&shown.getDisplay().getDisplayId()==d.getDisplayId())return;

        close();try{shown=new Output(a,d);owner=new WeakReference<Activity>(a);shown.show();shown.getWindow().setLayout(-1,-1);shown.getWindow().setGravity(Gravity.TOP|Gravity.LEFT);}

        catch(RuntimeException e){close();Toast.makeText(a,"无法打开仪表显示器："+e.getClass().getSimpleName(),Toast.LENGTH_LONG).show();}

    }

    /** HUD uses the same Presentation and Surface binding as the verified instrument path. */
    public static Presentation hudWindow(Activity a,Display d,Box area,int percent,boolean mirror,View overlay,Runnable start,Runnable stop){
        if(shown!=null&&shown.getDisplay().getDisplayId()==d.getDisplayId())close();
        return new Output(a,d,area,percent,mirror,overlay,start,stop);
    }

    private static String prefix(Display d){return "d"+d.getDisplayId()+"_";}

    private static Box readBox(SharedPreferences p,String key,Box fallback){return LayoutGeometry.valid(new Box(p.getFloat(key+"l",fallback.l),p.getFloat(key+"t",fallback.t),p.getFloat(key+"r",fallback.r),p.getFloat(key+"b",fallback.b)));}

    private static void saveBox(SharedPreferences.Editor e,String key,Box b){e.putFloat(key+"l",b.l).putFloat(key+"t",b.t).putFloat(key+"r",b.r).putFloat(key+"b",b.b);}

    private static Box defaultMap(){return new Box(0f,.04f,1f,1f);}

    private static void upgradeMap(Context c){SharedPreferences p=prefs(c);if(!p.getBoolean("enabled",false)||!p.getBoolean("map_on",true))return;int display=p.getInt("display",-1);if(display<0)return;String k="d"+display+"_";if(p.getBoolean(k+"default50",false))return;SharedPreferences.Editor e=p.edit().putBoolean(k+"default50",true);if(!p.getBoolean(k+"backup46",false)){e.putBoolean(k+"backup46",true).putBoolean(k+"before46_crop",p.getBoolean("crop",false));saveBox(e,k+"before46_map_",readBox(p,k+"map_",new Box(.03f,.08f,.69f,.92f)));}saveBox(e,k+"map_",defaultMap());e.putInt(k+"content_percent50",80);e.apply();MapBlend.applyReference(c,display);}

    private static Box defaultPrompt(){return new Box(.10f,.15f,.37f,.42f);}

    private static Box defaultMusic(){return new Box(.72f,.15f,.97f,.85f);}

    private static TextView text(Context c,String s,int size,int color){TextView t=new TextView(c);t.setText(s);t.setTextColor(color);t.setTextSize(size);return t;}

    private static Button button(Context c,String s){Button b=new Button(c);b.setText(s);b.setAllCaps(false);b.setTextSize(13);b.setTextColor(Color.WHITE);return b;}

    private static Spinner spinner(Context c,String[] list){Spinner s=new Spinner(c);ArrayAdapter<String> adapter=new ArrayAdapter<String>(c,android.R.layout.simple_spinner_dropdown_item,list){@Override public View getView(int position,View convertView,ViewGroup parent){View v=super.getView(position,convertView,parent);if(v instanceof TextView){((TextView)v).setTextColor(Color.WHITE);((TextView)v).setTextSize(13);}return v;}};s.setAdapter(adapter);return s;}

    public static void settings(final Activity a){

        final SharedPreferences p=prefs(a);final List<Display> displays=new ArrayList<Display>();

        for(Display d:((DisplayManager)a.getSystemService(Context.DISPLAY_SERVICE)).getDisplays())if(d.getDisplayId()!=Display.DEFAULT_DISPLAY)displays.add(d);

        if(displays.isEmpty()){new AlertDialog.Builder(a).setTitle("仪表布局").setMessage("未发现附加显示器，车机需要向应用开放仪表显示器。").setPositiveButton("知道了",null).show();return;}

        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(a,16),dp(a,8),dp(a,16),dp(a,8));body.setBackgroundColor(BG);

        TextView heading=text(a,"仪表布局 · 拖动移动，拉动边缘或角点缩放",18,Color.WHITE);body.addView(heading);

        String[] names=new String[displays.size()];int selected=0;

        for(int i=0;i<names.length;i++){Display d=displays.get(i);DisplayMetrics m=new DisplayMetrics();d.getRealMetrics(m);names[i]="显示器 "+d.getDisplayId()+" · "+d.getName()+" · "+m.widthPixels+"×"+m.heightPixels;if(p.getInt("display",-1)==d.getDisplayId())selected=i;}

        Spinner device=spinner(a,names);body.addView(device);

        final Editor canvas=new Editor(a,p);LinearLayout controls=new LinearLayout(a);controls.setGravity(Gravity.CENTER_VERTICAL);

        Button map=button(a,"地图范围"),music=button(a,"歌词"),promptButton=button(a,"导航提示"),preset=button(a,"默认布局");

        for(Button control:new Button[]{map,music,promptButton,preset}){control.setTextSize(12);controls.addView(control,new LinearLayout.LayoutParams(0,-2,1));}body.addView(controls);

        TextView selectedLabel=text(a,"当前调节：导航 · 重叠时只移动、缩放选中的框",13,BLUE);body.addView(selectedLabel);

        final SeekBar contentScale=new SeekBar(a);contentScale.setMax(100);TextView scaleLabel=text(a,"地图内容大小：80%",13,BLUE);

        Runnable mark=()->{int index=canvas.selection.selected();map.setText(index==0?"✓ 地图范围":"地图范围");music.setText(index==1?"✓ 歌词":"歌词");promptButton.setText(index==2?"✓ 导航提示":"导航提示");for(int i=0;i<3;i++){Button control=new Button[]{map,music,promptButton}[i];control.setBackground(panel(i==index?0xff235583:CARD,dp(a,10)));}selectedLabel.setText(new String[]{"当前调节：地图物理区域 · 内容缩放由下方滑条控制","当前调节：歌词区域 · 其他框保持不动","当前调节：导航提示区域 · 其他框保持不动"}[index]);canvas.invalidate();};

        map.setOnClickListener(v->{canvas.selection.select(0);mark.run();});music.setOnClickListener(v->{canvas.selection.select(1);mark.run();});promptButton.setOnClickListener(v->{canvas.selection.select(2);mark.run();});preset.setOnClickListener(v->{canvas.map=defaultMap();canvas.music=defaultMusic();canvas.prompt=defaultPrompt();canvas.contentPercent=80;contentScale.setProgress(40);mark.run();});mark.run();

        body.addView(canvas,new LinearLayout.LayoutParams(-1,dp(a,230)));

        body.addView(scaleLabel);body.addView(contentScale,new LinearLayout.LayoutParams(-1,dp(a,36)));contentScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int value,boolean user){canvas.contentPercent=MapViewport.percent(value+40);scaleLabel.setText("地图内容大小："+canvas.contentPercent+"% · 左边缩小，右边放大");canvas.invalidate();}public void onStartTrackingTouch(SeekBar bar){}public void onStopTrackingTouch(SeekBar bar){}});

        LinearLayout options=new LinearLayout(a);options.setGravity(Gravity.CENTER_VERTICAL);

        CheckBox mapOn=new CheckBox(a);mapOn.setText("地图");mapOn.setTextColor(Color.WHITE);mapOn.setChecked(p.getBoolean("map_on",true));

        CheckBox musicOn=new CheckBox(a);musicOn.setText("歌词 / 音乐");musicOn.setTextColor(Color.WHITE);musicOn.setChecked(p.getBoolean("music_on",true));

        options.addView(mapOn);options.addView(musicOn);body.addView(options);

        TextView fontLabel=text(a,"文字大小",13,MUTED);SeekBar font=new SeekBar(a);font.setMax(24);font.setProgress(Math.max(0,Math.min(24,p.getInt("font",22)-14)));

        LinearLayout fonts=new LinearLayout(a);fonts.setGravity(Gravity.CENTER_VERTICAL);fonts.addView(fontLabel);fonts.addView(font,new LinearLayout.LayoutParams(0,dp(a,36),1));Button colors=button(a,"歌名 / 歌词颜色");colors.setOnClickListener(v->MusicStyle.settings(a));fonts.addView(colors);body.addView(fonts);

        fontLabel.setText("文字 "+(font.getProgress()+14));font.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int n,boolean user){fontLabel.setText("文字 "+(n+14));}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});

        TextView hint=text(a,"地图范围框控制物理显示区域；滑条单独缩放地图内容，导航提示框独立移动和调整大小。这里只缩放投屏图像，不改变高德地图缩放级别；源画面之外保持透明。歌词取自手机发送的文字，无歌词时显示歌曲信息。",12,MUTED);body.addView(hint);

        Button shape=button(a,"地图形状 / 边缘融合 / 蓝色测试");body.addView(shape);shape.setOnClickListener(v->{int displayId=displays.get(device.getSelectedItemPosition()).getDisplayId();MapBlend.settings(a,displayId,()->{if(shown!=null&&shown.getDisplay().getDisplayId()==displayId&&shown.mapArea instanceof MapBlend.Layer)((MapBlend.Layer)shown.mapArea).refresh();},config->{if(shown!=null&&shown.getDisplay().getDisplayId()==displayId&&shown.mapArea instanceof MapBlend.Layer)((MapBlend.Layer)shown.mapArea).test(config);else Toast.makeText(a,"请先应用此显示器的地图布局，再进行蓝色区域测试",Toast.LENGTH_LONG).show();});});

        ScrollView scrolling=new ScrollView(a);scrolling.setFillViewport(true);scrolling.addView(body);final AlertDialog dialog=new AlertDialog.Builder(a).setView(scrolling).setPositiveButton("应用到仪表",null).setNeutralButton("关闭仪表输出",null).setNegativeButton("取消",null).create();

        device.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> parent,View v,int pos,long id){canvas.load(displays.get(pos));contentScale.setProgress(canvas.contentPercent-40);}public void onNothingSelected(AdapterView<?> parent){}});device.setSelection(selected);canvas.load(displays.get(selected));contentScale.setProgress(canvas.contentPercent-40);

        dialog.setOnShowListener(unused->{dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setLayout(-1,-1);

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{p.edit().putBoolean("enabled",false).apply();close();dialog.dismiss();});

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{

                if(!mapOn.isChecked()&&!musicOn.isChecked()){Toast.makeText(a,"请至少选择地图或歌词 / 音乐",Toast.LENGTH_SHORT).show();return;}

                Display d=displays.get(device.getSelectedItemPosition());String key=prefix(d);SharedPreferences.Editor e=p.edit().putBoolean("map_upgrade46",true).putBoolean("enabled",true).putInt("display",d.getDisplayId()).putBoolean("map_on",mapOn.isChecked()).putBoolean("music_on",musicOn.isChecked()).putInt("font",font.getProgress()+14);

                e.putBoolean(key+"default50",true);saveBox(e,key+"map_",canvas.map);saveBox(e,key+"music_",canvas.music);saveBox(e,key+"prompt_",canvas.prompt);e.putInt(key+"content_percent50",canvas.contentPercent);e.apply();

                SharedPreferences original=a.getSharedPreferences("xcertplay_airplay",0);boolean reconnect=mapOn.isChecked()&&!original.getBoolean("cluster_map_enabled",false);

                if(mapOn.isChecked())original.edit().putBoolean("cluster_map_enabled",true).putString("cluster_content","MAP").apply();

                if(!p.getBoolean(key+"blend_default48",false)){MapBlend.applyReference(a,d.getDisplayId());p.edit().putBoolean(key+"blend_default48",true).apply();}close();restore(a);AppOutput.refresh(a);dialog.dismiss();if(reconnect)Toast.makeText(a,"已保存。首次启用地图流请在 DiPlay 中重新连接 CarPlay",Toast.LENGTH_LONG).show();

            });

        });dialog.show();

    }



    private static final class Editor extends View {

        private final SharedPreferences p;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);

        private final RectF screen=new RectF();Box map=defaultMap(),music=defaultMusic(),prompt=defaultPrompt(),start;final EditSelection selection=new EditSelection(3);int contentPercent=80,referenceW,referenceH;boolean referenceCrop;int edges;float downX,downY;boolean dragging;int displayW=1,displayH=1;

        Editor(Context c,SharedPreferences p){super(c);this.p=p;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}

        void load(Display d){DisplayMetrics m=new DisplayMetrics();d.getRealMetrics(m);displayW=Math.max(1,m.widthPixels);displayH=Math.max(1,m.heightPixels);String key=prefix(d);map=readBox(p,key+"map_",defaultMap());music=readBox(p,key+"music_",defaultMusic());prompt=readBox(p,key+"prompt_",defaultPrompt());contentPercent=MapViewport.percent(p.getInt(key+"content_percent50",80));int[] ref=LayoutGeometry.pixels(readBox(p,key+"before46_map_",new Box(.03f,.08f,.69f,.92f)),displayW,displayH);referenceW=ref[2];referenceH=ref[3];referenceCrop=p.getBoolean(key+"before46_crop",false);invalidate();}

        private RectF rect(Box b){return new RectF(screen.left+b.l*screen.width(),screen.top+b.t*screen.height(),screen.left+b.r*screen.width(),screen.top+b.b*screen.height());}

        @Override protected void onDraw(Canvas c){super.onDraw(c);float pad=dp(getContext(),22),w=Math.max(1,getWidth()-pad*2),h=Math.max(1,getHeight()-pad*2);float scale=Math.min(w/displayW,h/displayH);float sw=displayW*scale,sh=displayH*scale;screen.set((getWidth()-sw)/2,(getHeight()-sh)/2,(getWidth()+sw)/2,(getHeight()+sh)/2);

            paint.setStyle(Paint.Style.FILL);paint.setColor(0xff02060b);c.drawRoundRect(screen,dp(getContext(),12),dp(getContext(),12),paint);

            paint.setColor(0xff1c2938);paint.setStrokeWidth(1);for(int i=1;i<8;i++){float x=screen.left+screen.width()*i/8;c.drawLine(x,screen.top,x,screen.bottom,paint);}for(int i=1;i<4;i++){float y=screen.top+screen.height()*i/4;c.drawLine(screen.left,y,screen.right,y,paint);}

            Box[] boxes={map,music,prompt};for(int i=0;i<3;i++)if(i!=selection.selected())drawBox(c,boxes[i],i);drawBox(c,boxes[selection.selected()],selection.selected());

            RectF r=rect(new Box[]{map,music,prompt}[selection.selected()]);paint.setColor(selection.selected()==0?BLUE:MINT);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(getContext(),2));c.drawRoundRect(r,dp(getContext(),10),dp(getContext(),10),paint);paint.setStyle(Paint.Style.FILL);

            float[] xs={r.left,r.centerX(),r.right},ys={r.top,r.centerY(),r.bottom};for(int xi=0;xi<3;xi++)for(int yi=0;yi<3;yi++)if(xi!=1||yi!=1)c.drawCircle(xs[xi],ys[yi],dp(getContext(),6),paint);

        }

        private void drawBox(Canvas c,Box b,int index){RectF r=rect(b);int save=c.save();c.clipRect(r);paint.setStyle(Paint.Style.FILL);

            if(index==2){GaodeCard.draw(c,r,2,"300 米","下一道路");}

            else if(index==0){paint.setColor(0xff102434);c.drawRect(r,paint);int[] render=MapViewport.render(referenceW,referenceH,MapMirrors.INSTANCE.getStreamAspect(),referenceCrop,contentPercent);float w=render[0]*screen.width()/displayW,h=render[1]*screen.height()/displayH;RectF image=new RectF(r.centerX()-w/2,r.centerY()-h/2,r.centerX()+w/2,r.centerY()+h/2);paint.setColor(0xff192e40);c.drawRect(image,paint);int clip=c.save();c.clipRect(image);paint.setColor(0xff426276);paint.setStrokeWidth(dp(getContext(),3));c.drawLine(image.left,image.top+image.height()*.7f,image.right,image.top+image.height()*.2f,paint);paint.setColor(BLUE);paint.setStrokeWidth(dp(getContext(),2));c.drawLine(image.left+image.width()*.2f,image.bottom,image.centerX(),image.centerY(),paint);c.drawLine(image.centerX(),image.centerY(),image.right,image.centerY(),paint);c.restoreToCount(clip);}

            paint.setColor(index==1?MINT:Color.WHITE);paint.setTextSize(Math.min(dp(getContext(),16),r.height()*.2f));paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setTextAlign(Paint.Align.CENTER);if(index!=2)c.drawText(index==0?"地图物理区域":"歌词 / 音乐",r.centerX(),r.centerY(),paint);paint.setTypeface(Typeface.DEFAULT);c.restoreToCount(save);

        }

        @Override public boolean onTouchEvent(MotionEvent event){

            float x=event.getX(),y=event.getY();if(screen.width()<1||screen.height()<1)return false;

            if(event.getActionMasked()==MotionEvent.ACTION_DOWN){

                float hit=dp(getContext(),20);

                if(!selection.begin(new Box[]{map,music,prompt},(x-screen.left)/screen.width(),(y-screen.top)/screen.height(),hit/screen.width(),hit/screen.height()))return false;

                RectF active=rect(new Box[]{map,music,prompt}[selection.target()]);

                edges=0;float horizontal=Math.min(hit,active.width()*.25f),vertical=Math.min(hit,active.height()*.25f);

                if(Math.abs(x-active.left)<=horizontal)edges|=1;else if(Math.abs(x-active.right)<=horizontal)edges|=4;

                if(Math.abs(y-active.top)<=vertical)edges|=2;else if(Math.abs(y-active.bottom)<=vertical)edges|=8;

                start=new Box[]{map,music,prompt}[selection.target()].copy();downX=x;downY=y;dragging=true;getParent().requestDisallowInterceptTouchEvent(true);invalidate();return true;

            }

            if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&dragging){Box next=LayoutGeometry.resize(start,edges,(x-downX)/screen.width(),(y-downY)/screen.height());if(selection.target()==0)map=next;else if(selection.target()==1)music=next;else prompt=next;invalidate();return true;}

            if(event.getActionMasked()==MotionEvent.ACTION_UP||event.getActionMasked()==MotionEvent.ACTION_CANCEL){boolean handled=dragging;dragging=false;selection.end();getParent().requestDisallowInterceptTouchEvent(false);if(event.getActionMasked()==MotionEvent.ACTION_UP&&handled)performClick();return handled;}return dragging;

        }

        @Override public boolean performClick(){super.performClick();return true;}

    }



    private static final class Output extends Presentation implements TextureView.SurfaceTextureListener {

        private final Handler main=new Handler(Looper.getMainLooper());private final String key="menglong-layout-"+System.identityHashCode(this);

        private Surface surface;private SurfaceTexture bound;private FrameLayout mapArea;private TextureView texture;private NavPrompt.Overlay prompt;

        private TextView title,artist,line;private ImageView cover;private Bitmap lastCover;private int musicW,musicH,font;private boolean crop;private int referenceW,referenceH,contentPercent;

        private final Activity activity;private final boolean hudMode;private final Box hudArea;private final int hudPercent;private final boolean hudMirror;private final View hudOverlay;private final Runnable hudStart,hudStop;private long hudFrames,hudLastFrame;private boolean hudStyleNight;private HudMapEnhancer hudEnhancer;private InstrumentNightEnhancer instrumentEnhancer;
        Output(Context c,Display d){super(c,d);activity=(Activity)c;hudMode=false;hudArea=null;hudPercent=100;hudMirror=false;hudOverlay=null;hudStart=null;hudStop=null;}
        Output(Activity a,Display d,Box area,int percent,boolean mirror,View overlay,Runnable start,Runnable stop){super(a,d);activity=a;hudMode=true;hudArea=area;hudPercent=percent;hudMirror=mirror;hudOverlay=overlay;hudStart=start;hudStop=stop;}


        @Override protected void onCreate(Bundle b){super.onCreate(b);getWindow().setBackgroundDrawableResource(android.R.color.transparent);getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            SharedPreferences p=prefs(getContext());DisplayMetrics m=new DisplayMetrics();getDisplay().getRealMetrics(m);String prefix=prefix(getDisplay());FrameLayout root=new FrameLayout(getContext());crop=p.getBoolean(prefix+"before46_crop",false);font=Math.max(14,Math.min(38,p.getInt("font",22)));contentPercent=MapViewport.percent(p.getInt(prefix+"content_percent50",80));

            if(hudMode){
                crop=false;contentPercent=HudFocus.percent(hudPercent);int[] area=LayoutGeometry.pixels(hudArea,m.widthPixels,m.heightPixels);referenceW=area[2];referenceH=area[3];
                mapArea=new V3HudFrame(getContext());mapArea.setClipChildren(true);mapArea.setBackgroundColor(Color.TRANSPARENT);if(hudMirror)mapArea.setScaleX(-1);root.addView(mapArea,position(hudArea,m));
                texture=new TextureView(getContext());hudStyleNight=NightTheme.isNight();texture.setOpaque(true);texture.setSurfaceTextureListener(this);int[] render=V3HudCrop.active(getContext())?MapViewport.render(referenceW,referenceH,MapMirrors.INSTANCE.getStreamAspect(),false,100):HudFocus.render(referenceW,referenceH,MapMirrors.INSTANCE.getStreamAspect(),contentPercent);mapArea.addView(texture,new FrameLayout.LayoutParams(render[0],render[1],Gravity.CENTER));
                HudMap.status="显示器 "+getDisplay().getDisplayId()+" · 仪表地图路径等待帧";
            }else if(p.getBoolean("map_on",true)){

                mapArea=new MapBlend.Layer(getContext(),getDisplay().getDisplayId());mapArea.setBackgroundColor(Color.TRANSPARENT);root.addView(mapArea,position(readBox(p,prefix+"map_",defaultMap()),m));

                Box reference=readBox(p,prefix+"before46_map_",new Box(.03f,.08f,.69f,.92f));int[] referencePixels=LayoutGeometry.pixels(reference,m.widthPixels,m.heightPixels);referenceW=referencePixels[2];referenceH=referencePixels[3];

                texture=new TextureView(getContext());texture.setOpaque(false);texture.setSurfaceTextureListener(this);int[] render=MapViewport.render(referenceW,referenceH,MapMirrors.INSTANCE.getStreamAspect(),crop,contentPercent);mapArea.addView(texture,new FrameLayout.LayoutParams(render[0],render[1],Gravity.CENTER));

            }

            if(!hudMode&&p.getBoolean("music_on",true)){

                int[] area=LayoutGeometry.pixels(readBox(p,prefix+"music_",defaultMusic()),m.widthPixels,m.heightPixels);musicW=area[2];musicH=area[3];

                LinearLayout music=new LinearLayout(getContext());music.setOrientation(LinearLayout.VERTICAL);music.setGravity(Gravity.CENTER_VERTICAL);music.setBackgroundColor(Color.TRANSPARENT);int pad=Math.max(2,Math.round(Math.min(musicW,musicH)*.07f));music.setPadding(pad,pad,pad,pad);root.addView(music,position(readBox(p,prefix+"music_",defaultMusic()),m));

                LinearLayout header=new LinearLayout(getContext());header.setGravity(Gravity.CENTER_VERTICAL);music.addView(header,new LinearLayout.LayoutParams(-1,0,1.2f));

                cover=new ImageView(getContext());cover.setScaleType(ImageView.ScaleType.CENTER_CROP);int size=Math.max(1,Math.min((musicW-pad*2)/3,(musicH-pad*2)/3));cover.setBackground(panel(0xff223247,size*.15f));cover.setClipToOutline(true);header.addView(cover,new LinearLayout.LayoutParams(size,size));

                LinearLayout words=new LinearLayout(getContext());words.setOrientation(LinearLayout.VERTICAL);LinearLayout.LayoutParams wordp=new LinearLayout.LayoutParams(0,-2,1);wordp.leftMargin=pad;header.addView(words,wordp);

                title=musicText(Color.WHITE,true);title.setMaxLines(2);artist=musicText(MUTED,false);artist.setMaxLines(1);words.addView(title);words.addView(artist);

                line=musicText(MINT,true);line.setGravity(Gravity.CENTER_VERTICAL);line.setMaxLines(3);music.addView(line,new LinearLayout.LayoutParams(-1,0,1.3f));

                fitText();applyMusicStyle();

            }if(!hudMode&&mapArea!=null){prompt=new NavPrompt.Overlay(getContext(),activity,getDisplay().getDisplayId());root.addView(prompt,new FrameLayout.LayoutParams(-1,-1));}if(hudOverlay!=null)root.addView(hudOverlay,new FrameLayout.LayoutParams(-1,-1));setContentView(root);

        }

        private FrameLayout.LayoutParams position(Box b,DisplayMetrics m){int[] p=LayoutGeometry.pixels(b,m.widthPixels,m.heightPixels);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(p[2],p[3]);lp.leftMargin=p[0];lp.topMargin=p[1];return lp;}

        private TextView musicText(int color,boolean bold){TextView t=text(getContext(),"",font,color);t.setIncludeFontPadding(false);t.setEllipsize(TextUtils.TruncateAt.END);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}

        private void fitText(){float scaled=getContext().getResources().getDisplayMetrics().scaledDensity;float px=Math.min(font*scaled,Math.min(musicH*.12f,musicW*.08f));px=Math.max(1,px);title.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px);artist.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px*.7f);line.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px*1.1f);}

        private void applyMusicStyle(){title.setTextColor(MusicStyle.title(getContext()));artist.setTextColor(MusicStyle.title(getContext()));line.setTextColor(MusicStyle.lyric(getContext()));}

        private void put(TextView t,String s){if(!TextUtils.equals(t.getText(),s))t.setText(s);}

        private void update(){

            if(!ProjectionGate.visible(activity)){ProjectionGate.clear();return;}

            NightTheme.refresh(activity);if(hudEnhancer!=null)hudEnhancer.setNight(NightTheme.isNight());if(instrumentEnhancer!=null)instrumentEnhancer.setNight(NightTheme.isNight());if(prompt!=null)prompt.update();if(hudMode&&texture!=null&&hudStyleNight!=NightTheme.isNight()){hudStyleNight=NightTheme.isNight();texture.setOpaque(true);}

            if(mapArea!=null){if(mapArea instanceof MapBlend.Layer)((MapBlend.Layer)mapArea).refresh();if(hudMode)HudMap.status="显示器 "+getDisplay().getDisplayId()+" · "+(hudFrames==0?"仪表地图路径等待帧":android.os.SystemClock.elapsedRealtime()-hudLastFrame>3000?"已收到地图帧，等待更新":"仪表地图路径实时输出");double aspect=MapMirrors.INSTANCE.getStreamAspect();int w=mapArea.getWidth(),h=mapArea.getHeight();if(w>0&&h>0&&aspect>0&&Double.isFinite(aspect)){

                int[] render=hudMode?(V3HudCrop.active(getContext())?MapViewport.render(referenceW,referenceH,aspect,false,100):HudFocus.render(referenceW,referenceH,aspect,contentPercent)):MapViewport.render(referenceW,referenceH,aspect,crop,contentPercent);int tw=render[0],th=render[1];ViewGroup.LayoutParams old=texture.getLayoutParams();if(old.width!=tw||old.height!=th)texture.setLayoutParams(new FrameLayout.LayoutParams(tw,th,Gravity.CENTER));

            }if(hudMode){V3HudCrop.apply(texture);if(mapArea instanceof V3HudFrame)((V3HudFrame)mapArea).refresh();}}

            if(title!=null){MediaSession session=CarPlayMediaKeys.menglongSession();MediaController c=session==null?null:session.getController();MediaMetadata meta=c==null?null:c.getMetadata();

                String name=meta==null?null:meta.getString(MediaMetadata.METADATA_KEY_TITLE),person=meta==null?null:meta.getString(MediaMetadata.METADATA_KEY_ARTIST);CarPlayGlance.Snapshot glance=CarPlayGlance.INSTANCE.snapshot();String phoneText=glance!=null&&glance.getConnected()?CarPlayGlance.INSTANCE.menglongSongLine():null;

                if(meta!=null){name=MusicPalette.first(name,meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE));person=MusicPalette.first(person,meta.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST));person=MusicPalette.first(person,meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE));}

                MusicText.Rows rows=MusicText.separate(name,person,phoneText);

                put(title,rows.title);title.setVisibility(rows.title.isEmpty()?View.GONE:View.VISIBLE);put(artist,rows.artist);put(line,rows.lyrics);artist.setVisibility(rows.artist.isEmpty()?View.GONE:View.VISIBLE);line.setVisibility(rows.lyrics.isEmpty()?View.GONE:View.VISIBLE);

                applyMusicStyle();title.setShadowLayer(0,0,0,Color.TRANSPARENT);artist.setShadowLayer(0,0,0,Color.TRANSPARENT);line.setShadowLayer(0,0,0,Color.TRANSPARENT);

                Bitmap art=meta==null?null:meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);if(art==null&&meta!=null)art=meta.getBitmap(MediaMetadata.METADATA_KEY_ART);if(art!=lastCover){cover.setImageBitmap(art);lastCover=art;}cover.setVisibility(art==null?View.GONE:View.VISIBLE);

            }

        }

        private final Runnable tick=new Runnable(){public void run(){if(!isShowing())return;if(UiDiagnostics.guard(getContext(),"仪表更新（地图 / 歌词）",()->update()))main.postDelayed(this,500);else failed();}};

        @Override protected void onStart(){super.onStart();if(hudStart!=null)hudStart.run();main.post(tick);}

        @Override protected void onStop(){main.removeCallbacksAndMessages(null);if(hudStop!=null)hudStop.run();if(prompt!=null)prompt.stop();release();if(hudMode)HudMap.status="显示器 "+getDisplay().getDisplayId()+" · 地图已关闭";super.onStop();if(shown==this){shown=null;owner.clear();}}

        private void failed(){main.removeCallbacksAndMessages(null);(hudMode?getContext().getSharedPreferences("menglong_hud_214",0):prefs(getContext())).edit().putBoolean("enabled",false).apply();main.post(()->UiDiagnostics.guard(getContext(),"关闭失败的仪表输出",()->dismiss()));UiDiagnostics.guard(getContext(),"显示仪表异常提示",()->Toast.makeText(getContext().getApplicationContext(),"仪表输出异常，已暂停；请打开故障记录",Toast.LENGTH_LONG).show());}

        private void release(){UiDiagnostics.guard(getContext(),"移除仪表地图镜像",()->MapMirrors.INSTANCE.set(key,null));if(hudEnhancer!=null){hudEnhancer.close();hudEnhancer=null;}if(instrumentEnhancer!=null){instrumentEnhancer.close();instrumentEnhancer=null;}Surface old=surface;surface=null;bound=null;hudFrames=0;hudLastFrame=0;if(old!=null)UiDiagnostics.guard(getContext(),"释放仪表地图 Surface",()->old.release());}

        public void onSurfaceTextureAvailable(SurfaceTexture t,int w,int h){if(!UiDiagnostics.guard(getContext(),"绑定仪表地图 Surface",()->{release();bound=t;surface=new Surface(t);if(hudMode)MapMirrors.INSTANCE.set(key,surface);else instrumentEnhancer=new InstrumentNightEnhancer(getContext(),key,surface,w,h,true);}))failed();}

        public boolean onSurfaceTextureDestroyed(SurfaceTexture t){if(bound==t)release();return true;}

        public void onSurfaceTextureSizeChanged(SurfaceTexture t,int w,int h){}

        public void onSurfaceTextureUpdated(SurfaceTexture t){if(hudMode&&bound==t){hudFrames++;hudLastFrame=android.os.SystemClock.elapsedRealtime();if(hudFrames==1)HudMap.status="显示器 "+getDisplay().getDisplayId()+" · 仪表地图路径已收到帧";}}

    }

}

