package com.shilapi.xcertplay.menglong;

import android.content.Context;
import android.view.Surface;
import android.widget.Toast;
import com.shilapi.xcertplay.MapMirrors;

/** Owns only the GPU input; the caller continues owning its existing output Surface. */
public final class InstrumentNightEnhancer {
    private final Context context;
    private final String key;
    private final Surface output;
    private InstrumentNightGL renderer;
    private boolean closed;
    public InstrumentNightEnhancer(Context c,String key,Surface output,int w,int h){
        this(c,key,output,w,h,false);
    }
    public InstrumentNightEnhancer(Context c,String key,Surface output,int w,int h,boolean instrument){
        context=c;this.key=key;this.output=output;
        InstrumentNightDetail detail=instrument?new InstrumentNightDetail(1f,0f,false):new InstrumentNightDetail(c);
        renderer=new InstrumentNightGL(output,detail,w,h,new InstrumentNightGL.Listener(){
            public void ready(Surface input){if(!closed&&!bind(input))failed("无法绑定 HUD 滤镜输入");}
            public void failed(String message){
                UiDiagnostics.record(context,"HUD 地图细节滤镜",new IllegalStateException(message));
                InstrumentNightGL old=renderer;renderer=null;
                if(!closed)UiDiagnostics.guard(context,"移除失败 HUD 滤镜 Surface",()->MapMirrors.INSTANCE.set(key,null));
                if(old!=null)old.stop(()->{if(!closed){bind(output);Toast.makeText(context,"HUD 地图增强不可用，已恢复原地图显示",Toast.LENGTH_LONG).show();}});
            }
        });
    }
    private boolean bind(Surface s){return !closed&&UiDiagnostics.guard(context,"绑定 HUD 地图增强 Surface",()->MapMirrors.INSTANCE.set(key,s));}
    public void setNight(boolean night){if(renderer!=null)renderer.setNight(night);}
    public void close(){closed=true;InstrumentNightGL old=renderer;renderer=null;if(old!=null)old.stop();}
}
