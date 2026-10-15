package com.shilapi.xcertplay.menglong;

import android.graphics.SurfaceTexture;
import android.opengl.*;
import android.os.*;
import android.view.Surface;
import java.nio.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Small HUD-only GPU filter. The existing decoder writes to its input Surface. */
public final class HudDetailGL {
    public interface Listener { void ready(Surface input); void failed(String message); }
    private final Surface output;
    private final HudDetail detail;
    private final Listener listener;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final HandlerThread thread=new HandlerThread("HUD-map-detail");
    private final Handler gl;
    private final AtomicBoolean pending=new AtomicBoolean();
    private volatile boolean stopped, night;
    private boolean failed;
    private EGLDisplay display=EGL14.EGL_NO_DISPLAY;
    private EGLContext context=EGL14.EGL_NO_CONTEXT;
    private EGLSurface window=EGL14.EGL_NO_SURFACE;
    private SurfaceTexture inputTexture;
    private Surface input;
    private int program, textureId;
    private int positionLoc,uvLoc,matrixLoc,stepLoc,contrastLoc,sharpLoc,boldLoc,nightLoc;
    private final float[] transform=new float[16];
    private final FloatBuffer vertices=buffer(new float[]{-1,-1,1,-1,-1,1,1,1});
    private final FloatBuffer uv=buffer(new float[]{0,0,1,0,0,1,1,1});
    private final int[] width=new int[1],height=new int[1];

    private static final String VERTEX=
        "attribute vec2 aPosition;attribute vec2 aUv;uniform mat4 uMatrix;varying vec2 vUv;"+
        "void main(){gl_Position=vec4(aPosition,0.0,1.0);vUv=(uMatrix*vec4(aUv,0.0,1.0)).xy;}";
    private static final String FRAGMENT=
        "#extension GL_OES_EGL_image_external : require\nprecision mediump float;"+
        "uniform samplerExternalOES uTexture;uniform vec2 uStep;"+
        "uniform float uContrast,uSharp,uBold,uNight;varying vec2 vUv;"+
        "float ink(vec3 c){float l=dot(c,vec3(.299,.587,.114));return mix(1.0-l,l,uNight);}"+
        "vec3 sampleAt(vec2 p){return texture2D(uTexture,clamp(p,vec2(0.001),vec2(.999))).rgb;}"+
        "void main(){vec3 c=sampleAt(vUv);vec3 l=sampleAt(vUv-vec2(uStep.x,0.0));"+
        "vec3 r=sampleAt(vUv+vec2(uStep.x,0.0));vec3 t=sampleAt(vUv+vec2(0.0,uStep.y));"+
        "vec3 b=sampleAt(vUv-vec2(0.0,uStep.y));vec3 avg=(l+r+t+b)*.25;"+
        "float edge=max(max(length(c-l),length(c-r)),max(length(c-t),length(c-b)));"+
        "float mask=smoothstep(.025,.20,edge);"+
        "vec3 contrasted=clamp((c-.5)*uContrast+.5,0.0,1.0);"+
        "vec3 outColor=mix(c,contrasted,mask);"+
        "outColor=clamp(outColor+uSharp*.65*(c-avg)*mask,0.0,1.0);"+
        "vec3 best=c;float bestInk=ink(c);"+
        "if(ink(l)>bestInk){best=l;bestInk=ink(l);}if(ink(r)>bestInk){best=r;bestInk=ink(r);}"+
        "if(ink(t)>bestInk){best=t;bestInk=ink(t);}if(ink(b)>bestInk){best=b;bestInk=ink(b);}"+
        "float grow=uBold*.55*smoothstep(.06,.25,bestInk-ink(c))*smoothstep(.18,.4,bestInk);"+
        "outColor=mix(outColor,best,grow);gl_FragColor=vec4(outColor,1.0);}";

    public HudDetailGL(Surface output,HudDetail detail,int w,int h,Listener listener){
        this.output=output;this.detail=detail;this.listener=listener;night=NightTheme.isNight();
        thread.start();gl=new Handler(thread.getLooper());gl.post(()->init(w,h));
    }
    private static FloatBuffer buffer(float[] data){FloatBuffer b=ByteBuffer.allocateDirect(data.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();b.put(data).position(0);return b;}
    public void setNight(boolean value){night=value;}
    public void stop(){stop(null);}
    public void stop(Runnable after){if(stopped)return;stopped=true;gl.post(()->{cleanup();thread.quitSafely();if(after!=null)ui.post(after);});}
    private void init(int w,int h){
        if(stopped)return;
        try{
            display=EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);int[] version=new int[2];
            check(display!=EGL14.EGL_NO_DISPLAY&&EGL14.eglInitialize(display,version,0,version,1),"EGL 初始化");
            int[] attrs={EGL14.EGL_RED_SIZE,8,EGL14.EGL_GREEN_SIZE,8,EGL14.EGL_BLUE_SIZE,8,EGL14.EGL_ALPHA_SIZE,8,EGL14.EGL_RENDERABLE_TYPE,EGL14.EGL_OPENGL_ES2_BIT,EGL14.EGL_SURFACE_TYPE,EGL14.EGL_WINDOW_BIT,EGL14.EGL_NONE};
            EGLConfig[] configs=new EGLConfig[1];int[] count=new int[1];
            check(EGL14.eglChooseConfig(display,attrs,0,configs,0,1,count,0)&&count[0]>0,"EGL 配置");
            context=EGL14.eglCreateContext(display,configs[0],EGL14.EGL_NO_CONTEXT,new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION,2,EGL14.EGL_NONE},0);
            check(context!=EGL14.EGL_NO_CONTEXT,"EGL 上下文");
            window=EGL14.eglCreateWindowSurface(display,configs[0],output,new int[]{EGL14.EGL_NONE},0);
            check(window!=EGL14.EGL_NO_SURFACE&&EGL14.eglMakeCurrent(display,window,window,context),"EGL 输出窗口");
            int vs=shader(GLES20.GL_VERTEX_SHADER,VERTEX),fs=shader(GLES20.GL_FRAGMENT_SHADER,FRAGMENT);
            program=GLES20.glCreateProgram();GLES20.glAttachShader(program,vs);GLES20.glAttachShader(program,fs);GLES20.glLinkProgram(program);
            GLES20.glDeleteShader(vs);GLES20.glDeleteShader(fs);int[] ok=new int[1];GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,ok,0);
            check(ok[0]!=0,"滤镜链接："+GLES20.glGetProgramInfoLog(program));
            positionLoc=GLES20.glGetAttribLocation(program,"aPosition");uvLoc=GLES20.glGetAttribLocation(program,"aUv");
            matrixLoc=uniform("uMatrix");stepLoc=uniform("uStep");contrastLoc=uniform("uContrast");sharpLoc=uniform("uSharp");boldLoc=uniform("uBold");nightLoc=uniform("uNight");
            int[] ids=new int[1];GLES20.glGenTextures(1,ids,0);textureId=ids[0];GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            inputTexture=new SurfaceTexture(textureId);inputTexture.setDefaultBufferSize(Math.max(1,w),Math.max(1,h));
            inputTexture.setOnFrameAvailableListener(t->{if(!stopped&&pending.compareAndSet(false,true))gl.post(draw);},gl);
            input=new Surface(inputTexture);ui.post(()->{if(!stopped)listener.ready(input);});
        }catch(Throwable e){fail(e);}
    }
    private final Runnable draw=this::drawFrame;
    private void drawFrame(){
        pending.set(false);if(stopped||failed)return;
        try{
            inputTexture.updateTexImage();inputTexture.getTransformMatrix(transform);
            check(EGL14.eglQuerySurface(display,window,EGL14.EGL_WIDTH,width,0)&&EGL14.eglQuerySurface(display,window,EGL14.EGL_HEIGHT,height,0),"HUD 输出尺寸");
            int w=Math.max(1,width[0]),h=Math.max(1,height[0]);GLES20.glViewport(0,0,w,h);GLES20.glUseProgram(program);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);
            GLES20.glUniform1i(uniform("uTexture"),0);GLES20.glUniformMatrix4fv(matrixLoc,1,false,transform,0);
            GLES20.glUniform2f(stepLoc,1f/w,1f/h);GLES20.glUniform1f(contrastLoc,detail.contrast);GLES20.glUniform1f(sharpLoc,detail.sharpness);
            GLES20.glUniform1f(boldLoc,detail.bold?1:0);GLES20.glUniform1f(nightLoc,night?1:0);
            vertices.position(0);uv.position(0);GLES20.glEnableVertexAttribArray(positionLoc);GLES20.glEnableVertexAttribArray(uvLoc);
            GLES20.glVertexAttribPointer(positionLoc,2,GLES20.GL_FLOAT,false,0,vertices);GLES20.glVertexAttribPointer(uvLoc,2,GLES20.GL_FLOAT,false,0,uv);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);check(GLES20.glGetError()==GLES20.GL_NO_ERROR,"HUD 滤镜绘制");
            check(EGL14.eglSwapBuffers(display,window),"HUD 滤镜提交");
        }catch(Throwable e){fail(e);}
    }
    private int uniform(String n){return GLES20.glGetUniformLocation(program,n);}
    private int shader(int type,String code){int s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,code);GLES20.glCompileShader(s);int[] ok=new int[1];GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,ok,0);if(ok[0]==0){String log=GLES20.glGetShaderInfoLog(s);GLES20.glDeleteShader(s);throw new IllegalStateException(log);}return s;}
    private void check(boolean condition,String text){if(!condition)throw new IllegalStateException(text);}
    private void fail(Throwable e){if(stopped||failed)return;failed=true;String message=e.getClass().getSimpleName()+": "+e.getMessage();ui.post(()->{if(!stopped)listener.failed(message);});}
    private void cleanup(){
        try{if(inputTexture!=null)inputTexture.setOnFrameAvailableListener(null);if(input!=null)input.release();if(inputTexture!=null)inputTexture.release();
            if(display!=EGL14.EGL_NO_DISPLAY){if(context!=EGL14.EGL_NO_CONTEXT&&window!=EGL14.EGL_NO_SURFACE)EGL14.eglMakeCurrent(display,window,window,context);
                if(program!=0)GLES20.glDeleteProgram(program);if(textureId!=0)GLES20.glDeleteTextures(1,new int[]{textureId},0);
                EGL14.eglMakeCurrent(display,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT);
                if(window!=EGL14.EGL_NO_SURFACE)EGL14.eglDestroySurface(display,window);if(context!=EGL14.EGL_NO_CONTEXT)EGL14.eglDestroyContext(display,context);
                EGL14.eglTerminate(display);EGL14.eglReleaseThread();}
        }catch(Throwable ignored){}
    }
}
