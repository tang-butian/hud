package com.shilapi.xcertplay.menglong;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.display.DisplayManager;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import com.shilapi.xcertplay.glance.CarPlayGlance;
import com.shilapi.xcertplay.menglong.LayoutGeometry.Box;
import java.lang.ref.WeakReference;
import java.util.*;

/** Optional current-CarPlay video crop, independent of navigation and music output. */
public final class AppOutput {
    private static AppWindow window;
    private static WeakReference<Activity> owner=new WeakReference<Activity>(null);
    private static WeakReference<Activity> foreground=new WeakReference<Activity>(null);
    private static SharedPreferences prefs(Context c){return c.getSharedPreferences("menglong_app_video_214",0);}
    private static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    private static Box full(){return new Box(0,0,1,1);}
    private static Box destination(){return new Box(.32f,.22f,.68f,.78f);}
    private static String key(Display d){return "d"+d.getDisplayId()+"_";}
    private static Box read(SharedPreferences p,String k,Box fallback){
        return LayoutGeometry.valid(new Box(p.getFloat(k+"l",fallback.l),p.getFloat(k+"t",fallback.t),p.getFloat(k+"r",fallback.r),p.getFloat(k+"b",fallback.b)));
    }
    private static void write(SharedPreferences.Editor e,String k,Box box){e.putFloat(k+"l",box.l).putFloat(k+"t",box.t).putFloat(k+"r",box.r).putFloat(k+"b",box.b);}
    private static TextView label(Context c,String text){TextView t=new TextView(c);t.setText(text);t.setTextSize(13);return t;}
    private static Button button(Context c,String text){Button b=new Button(c);b.setText(text);b.setTextSize(13);b.setAllCaps(false);return b;}

    public static void resumed(Activity a){
        foreground=new WeakReference<Activity>(a);
        restore(a);if(window!=null&&owner.get()==a&&window.view!=null)window.view.start();
    }
    public static void paused(Activity a){ProjectionGate.paused(a);NavPrompt.paused(a);if(foreground.get()==a)foreground.clear();if(owner.get()==a&&window!=null&&window.view!=null)window.view.suspend();}
    public static void destroyed(Activity a){ProjectionGate.destroyed(a);NavPrompt.paused(a);if(foreground.get()==a)foreground.clear();if(owner.get()==a)close();}
    public static void refresh(Activity a){if(owner.get()==a&&window!=null){close();restore(a);}}
    private static void close(){AppWindow old=window;window=null;owner.clear();if(old!=null)old.dismiss();}
    public static void clear(){close();}
    private static void restore(Activity a){
        if(!ProjectionGate.visible(a)){close();return;}
        SharedPreferences p=prefs(a);
        if(!p.getBoolean("enabled",false)){close();return;}
        Display d=((DisplayManager)a.getSystemService(Context.DISPLAY_SERVICE)).getDisplay(p.getInt("display",-1));
        if(d==null||d.getDisplayId()==Display.DEFAULT_DISPLAY){close();return;}
        if(window!=null&&window.isShowing()&&owner.get()==a&&window.getDisplay().getDisplayId()==d.getDisplayId())return;
        close();
        try{
            window=new AppWindow(a,d);owner=new WeakReference<Activity>(a);window.show();
            window.getWindow().setLayout(-1,-1);window.getWindow().setGravity(Gravity.TOP|Gravity.LEFT);
        }catch(RuntimeException e){close();Toast.makeText(a,"无法打开 App 投屏："+e.getClass().getSimpleName(),Toast.LENGTH_LONG).show();}
    }

    public static void settings(Activity a){
        SharedPreferences p=prefs(a);List<Display> list=new ArrayList<Display>();
        for(Display d:((DisplayManager)a.getSystemService(Context.DISPLAY_SERVICE)).getDisplays())if(d.getDisplayId()!=0)list.add(d);
        if(list.isEmpty()){new AlertDialog.Builder(a).setTitle("App 投屏").setMessage("未发现仪表或 HUD 显示器。").setPositiveButton("知道了",null).show();return;}
        LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(a,14),dp(a,6),dp(a,14),dp(a,6));
        body.addView(label(a,"CarPlay App 投屏 · 输出框与地图、歌词分别保存"));
        String[] names=new String[list.size()];int selected=0;
        for(int i=0;i<list.size();i++){
            Display d=list.get(i);DisplayMetrics m=new DisplayMetrics();d.getRealMetrics(m);
            names[i]="显示器 "+d.getDisplayId()+" · "+d.getName()+" · "+m.widthPixels+"×"+m.heightPixels;
            if(d.getDisplayId()==p.getInt("display",-1))selected=i;
        }
        Spinner devices=new Spinner(a);devices.setAdapter(new ArrayAdapter<String>(a,android.R.layout.simple_spinner_dropdown_item,names));body.addView(devices);
        BoxEditor editor=new BoxEditor(a,false);body.addView(label(a,"仪表位置与大小：拖动框内移动，拉动边缘或角点缩放"));
        body.addView(editor,new LinearLayout.LayoutParams(-1,0,1));
        final Box[] source={read(p,"source_",full())};TextView selectedSource=label(a,"");body.addView(selectedSource);
        Runnable mark=()->selectedSource.setText(source[0].l==0&&source[0].t==0&&source[0].r==1&&source[0].b==1?"来源：完整 CarPlay 画面":"来源：已选定 CarPlay 画面区域");mark.run();
        LinearLayout row=new LinearLayout(a);Button whole=button(a,"完整画面"),crop=button(a,"选取数据区域"),reset=button(a,"居中位置");row.addView(whole);row.addView(crop);row.addView(reset);body.addView(row);
        whole.setOnClickListener(v->{source[0]=full();mark.run();});crop.setOnClickListener(v->chooseSource(a,source,mark));reset.setOnClickListener(v->{editor.box=destination();editor.invalidate();});
        CheckBox fill=new CheckBox(a);fill.setText("铺满输出框（裁掉边缘）；不勾选为完整显示");fill.setTextSize(13);fill.setChecked(p.getBoolean("fill",false));body.addView(fill);
        body.addView(label(a,"先在 CarPlay 打开 Car Scanner 的数据页，再选取。输出跟随中控当前 App；切换 App 后画面也切换。中控需保持 CarPlay 界面打开。"));
        AlertDialog dialog=new AlertDialog.Builder(a).setView(body).setPositiveButton("显示到仪表",null).setNeutralButton("关闭 App 投屏",null).setNegativeButton("取消",null).create();
        devices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent,View v,int pos,long id){Display d=list.get(pos);DisplayMetrics m=new DisplayMetrics();d.getRealMetrics(m);editor.width=m.widthPixels;editor.height=m.heightPixels;editor.box=read(p,key(d)+"dest_",destination());editor.invalidate();}
            public void onNothingSelected(AdapterView<?> parent){}
        });devices.setSelection(selected);Display d=list.get(selected);DisplayMetrics metrics=new DisplayMetrics();d.getRealMetrics(metrics);editor.width=metrics.widthPixels;editor.height=metrics.heightPixels;editor.box=read(p,key(d)+"dest_",destination());
        dialog.setOnShowListener(unused->{dialog.getWindow().setLayout(-1,-1);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{p.edit().putBoolean("enabled",false).apply();close();dialog.dismiss();});
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                Display target=list.get(devices.getSelectedItemPosition());SharedPreferences.Editor e=p.edit().putBoolean("enabled",true).putInt("display",target.getDisplayId()).putBoolean("fill",fill.isChecked());
                write(e,key(target)+"dest_",editor.box);write(e,"source_",source[0]);e.apply();close();restore(a);dialog.dismiss();
                if(!a.getClass().getName().endsWith("CarPlayHostActivity"))Toast.makeText(a,"已保存。进入已连接的 CarPlay 界面后开始投屏",Toast.LENGTH_LONG).show();
            });
        });dialog.show();
    }

    private static void chooseSource(Activity a,Box[] source,Runnable mark){
        Capture capture=new Capture(a);capture.once(bitmap->{
            capture.stop();if(foreground.get()!=a||a.isFinishing()||a.isDestroyed())return;
            if(bitmap==null){Toast.makeText(a,"请先进入已连接的 CarPlay，并打开 Car Scanner 数据页，再选取区域",Toast.LENGTH_LONG).show();return;}
            BoxEditor editor=new BoxEditor(a,true);editor.image=bitmap;editor.width=bitmap.getWidth();editor.height=bitmap.getHeight();editor.box=source[0].copy();
            LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(a,12),dp(a,8),dp(a,12),dp(a,8));body.addView(label(a,"选取来源数据：拖动框内移动，拉动边缘或角点缩放。此处只改变来源，不改变仪表位置。"));body.addView(editor,new LinearLayout.LayoutParams(-1,0,1));
            Button whole=button(a,"选择完整画面");whole.setOnClickListener(v->{editor.box=full();editor.invalidate();});body.addView(whole);
            AlertDialog dialog=new AlertDialog.Builder(a).setTitle("选择 CarPlay 画面区域").setView(body).setPositiveButton("使用此区域",(di,which)->{source[0]=editor.box.copy();mark.run();}).setNegativeButton("取消",null).create();dialog.setOnShowListener(unused->dialog.getWindow().setLayout(-1,-1));dialog.show();
        });
    }

    private static final class BoxEditor extends View {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);final RectF screen=new RectF();
        final boolean source;Box box=destination(),start;Bitmap image;int width=1280,height=480,edges;float downX,downY;boolean dragging;
        BoxEditor(Context c,boolean source){super(c);this.source=source;}
        private RectF rectangle(){return new RectF(screen.left+box.l*screen.width(),screen.top+box.t*screen.height(),screen.left+box.r*screen.width(),screen.top+box.b*screen.height());}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float pad=dp(getContext(),22),scale=Math.min(Math.max(1,getWidth()-pad*2)/Math.max(1,width),Math.max(1,getHeight()-pad*2)/Math.max(1,height)),w=width*scale,h=height*scale;screen.set((getWidth()-w)/2,(getHeight()-h)/2,(getWidth()+w)/2,(getHeight()+h)/2);
            paint.setStyle(Paint.Style.FILL);paint.setColor(0xff101923);c.drawRect(screen,paint);
            if(image!=null)c.drawBitmap(image,null,screen,paint);RectF r=rectangle();
            paint.setColor(source?0x3376d9ff:0xff184c65);c.drawRect(r,paint);paint.setColor(0xff76d9ff);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(getContext(),2));c.drawRect(r,paint);paint.setStyle(Paint.Style.FILL);
            for(float x:new float[]{r.left,r.centerX(),r.right})for(float y:new float[]{r.top,r.centerY(),r.bottom})if(x!=r.centerX()||y!=r.centerY())c.drawCircle(x,y,dp(getContext(),5),paint);
            if(!source){paint.setColor(Color.WHITE);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(Math.min(dp(getContext(),18),r.height()*.15f));c.drawText("CarPlay App 画面",r.centerX(),r.centerY(),paint);}
        }
        @Override public boolean onTouchEvent(MotionEvent e){if(screen.width()<1||screen.height()<1)return false;float x=e.getX(),y=e.getY();
            if(e.getActionMasked()==MotionEvent.ACTION_DOWN){float hit=dp(getContext(),20);RectF r=rectangle();if(x<r.left-hit||x>r.right+hit||y<r.top-hit||y>r.bottom+hit)return false;float hx=Math.min(hit,r.width()*.25f),hy=Math.min(hit,r.height()*.25f);edges=0;if(Math.abs(x-r.left)<=hx)edges|=1;else if(Math.abs(x-r.right)<=hx)edges|=4;if(Math.abs(y-r.top)<=hy)edges|=2;else if(Math.abs(y-r.bottom)<=hy)edges|=8;start=box.copy();downX=x;downY=y;dragging=true;getParent().requestDisallowInterceptTouchEvent(true);return true;}
            if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&dragging){box=LayoutGeometry.resize(start,edges,(x-downX)/screen.width(),(y-downY)/screen.height());invalidate();return true;}
            if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){boolean handled=dragging;dragging=false;getParent().requestDisallowInterceptTouchEvent(false);if(handled&&e.getActionMasked()==MotionEvent.ACTION_UP)performClick();return handled;}return dragging;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }

    private interface Ready {void frame(Bitmap image);}
    /** Captures only this application's foreground CarPlay video, never the phone screen. */
    private static final class Capture {
        final WeakReference<Activity> activity;final Handler main=new Handler(Looper.getMainLooper());boolean pending,stopped;Bitmap front,back;
        Capture(Activity a){activity=new WeakReference<Activity>(a);}
        void stop(){stopped=true;front=null;back=null;}
        private View video(View v){
            View best=null;boolean valid=v instanceof TextureView?((TextureView)v).isAvailable():v instanceof SurfaceView&&((SurfaceView)v).getHolder().getSurface().isValid();
            if(valid&&v.isShown()&&v.getAlpha()>.05f&&v.getWidth()>0&&v.getHeight()>0)best=v;
            if(v instanceof ViewGroup){ViewGroup group=(ViewGroup)v;for(int i=0;i<group.getChildCount();i++){View candidate=video(group.getChildAt(i));if(candidate!=null&&(best==null||(long)candidate.getWidth()*candidate.getHeight()>(long)best.getWidth()*best.getHeight()))best=candidate;}}
            return best;
        }
        void once(Ready ready){
            if(stopped||pending)return;Activity a=activity.get();
            if(a==null||foreground.get()!=a||a.isFinishing()||a.isDestroyed()||!a.getClass().getName().endsWith("CarPlayHostActivity")){ready.frame(null);return;}
            CarPlayGlance.Snapshot state=CarPlayGlance.INSTANCE.snapshot();if(state==null||!state.getConnected()){ready.frame(null);return;}
            View v=video(a.getWindow().getDecorView());if(v==null){ready.frame(null);return;}
            float scale=Math.min(1,1280f/Math.max(v.getWidth(),v.getHeight()));int w=Math.max(1,Math.round(v.getWidth()*scale)),h=Math.max(1,Math.round(v.getHeight()*scale));
            if(back==null||back.getWidth()!=w||back.getHeight()!=h)back=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Bitmap target=back;pending=true;
            try{if(v instanceof TextureView)finish(((TextureView)v).getBitmap(target),ready);else PixelCopy.request((SurfaceView)v,target,result->finish(result==PixelCopy.SUCCESS?target:null,ready),main);}
            catch(RuntimeException ex){finish(null,ready);}
        }
        private void finish(Bitmap image,Ready ready){pending=false;if(stopped)return;if(!ProjectionGate.visible(activity.get())||foreground.get()!=activity.get())image=null;Bitmap old=front;front=image;back=old;ready.frame(image);}
    }

    private static final class AppWindow extends Presentation {
        final Activity activity;FrameView view;AppWindow(Activity a,Display d){super(a,d);activity=a;}
        @Override protected void onCreate(Bundle b){super.onCreate(b);getWindow().setBackgroundDrawableResource(android.R.color.transparent);getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);view=new FrameView(getContext(),activity,getDisplay());setContentView(view);}
        @Override protected void onStart(){super.onStart();view.start();}
        @Override protected void onStop(){view.stop();super.onStop();if(window==this){window=null;owner.clear();}}
    }
    private static final class FrameView extends View {
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);final Handler main=new Handler(Looper.getMainLooper());final Capture capture;final Box source,dest;final boolean fill;boolean running;Bitmap frame;
        FrameView(Context c,Activity a,Display d){super(c);capture=new Capture(a);SharedPreferences p=prefs(c);source=read(p,"source_",full());dest=read(p,key(d)+"dest_",destination());fill=p.getBoolean("fill",false);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
        void start(){if(running)return;running=true;main.post(tick);}
        void suspend(){running=false;main.removeCallbacksAndMessages(null);frame=null;invalidate();}
        void stop(){suspend();capture.stop();}
        final Runnable tick=new Runnable(){public void run(){if(!running)return;if(UiDiagnostics.guard(getContext(),"App 画面捕获",()->capture.once(image->{if(running){frame=image;invalidate();}})))main.postDelayed(this,200);else suspend();}};
        @Override protected void onDraw(Canvas c){super.onDraw(c);Bitmap image=frame;if(image==null||getWidth()<1||getHeight()<1)return;int[] d=LayoutGeometry.pixels(dest,getWidth(),getHeight());VideoRegion.Mapping m=VideoRegion.map(source,image.getWidth(),image.getHeight(),d[0],d[1],d[2],d[3],fill);int save=c.save();c.clipRect(d[0],d[1],d[0]+d[2],d[1]+d[3]);paint.setColor(Color.WHITE);c.drawBitmap(image,new Rect(m.source[0],m.source[1],m.source[0]+m.source[2],m.source[1]+m.source[3]),new RectF(m.left,m.top,m.right,m.bottom),paint);c.restoreToCount(save);}
    }
}
