package com.shilapi.xcertplay.menglong;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.util.*;

/** Private local failure report; shared only when the user presses Copy. */
public final class UiDiagnostics {
 private static boolean installed;private static final String FILE="menglong-display-diagnostic.txt";
 private static Context app;private static String last="";
 public static synchronized void install(Context context){
  if(installed)return;app=context.getApplicationContext();installed=true;
  final Thread.UncaughtExceptionHandler previous=Thread.getDefaultUncaughtExceptionHandler();
  Thread.setDefaultUncaughtExceptionHandler((thread,error)->{
   try{record(app,"未捕获异常 · "+thread.getName(),error);if(displayFailure(error))pausePreferences(app);}catch(Throwable ignored){}
   if(previous!=null)previous.uncaughtException(thread,error);else android.os.Process.killProcess(android.os.Process.myPid());
 });
 }
 private static boolean displayFailure(Throwable error){
  Set<Throwable> seen=Collections.newSetFromMap(new IdentityHashMap<Throwable,Boolean>());
  for(int depth=0;error!=null&&depth<6&&seen.add(error);depth++,error=error.getCause())for(StackTraceElement frame:error.getStackTrace())if(frame.getClassName().startsWith("com.shilapi.xcertplay.menglong."))return true;
  return false;
 }
 private static void pausePreferences(Context c){for(String name:new String[]{"menglong_instrument_214","menglong_hud_214","menglong_app_video_214"})c.getSharedPreferences(name,0).edit().putBoolean("enabled",false).commit();}
 public static boolean guard(Context c,String stage,Runnable operation){return UiGuard.run(operation,error->record(c,stage,error));}
 public static synchronized void record(Context context,String stage,Throwable error){
  String dedup=stage+":"+error.getClass().getName();if(dedup.equals(last))return;last=dedup;
  try{
   StringBuilder text=new StringBuilder("DiPlay 仪表诊断 · 0.2.18-menglong-all-test1（76）\n").append(new Date()).append("\n阶段：").append(stage).append("\nAndroid API：").append(Build.VERSION.SDK_INT).append("\n");
   Set<Throwable> seen=Collections.newSetFromMap(new IdentityHashMap<Throwable,Boolean>());
   for(int depth=0;error!=null&&depth<6&&seen.add(error);depth++,error=error.getCause()){
    text.append(depth==0?"异常：":"原因：").append(error.getClass().getName()).append('\n');
    // Only class names and stack locations; exception messages can contain phone data.
    StackTraceElement[] stack=error.getStackTrace();for(int i=0;i<Math.min(48,stack.length);i++)text.append("  at ").append(stack[i]).append('\n');
   }
   File file=new File(context.getApplicationContext().getFilesDir(),FILE);File temporary=new File(file.getParentFile(),FILE+".tmp");
   try(Writer writer=new OutputStreamWriter(new FileOutputStream(temporary),"UTF-8")){writer.write(text.toString());}
   if(!temporary.renameTo(file)){try(Writer writer=new OutputStreamWriter(new FileOutputStream(file),"UTF-8")){writer.write(text.toString());}temporary.delete();}
  }catch(IOException|RuntimeException ignored){}
 }
 private static String report(Context c){
  File file=new File(c.getFilesDir(),FILE);if(!file.isFile())return "尚无故障记录。\n若仍闪退，请重新打开应用后点此处复制记录。\n本记录只包含异常类型与调用位置，不包含异常消息、歌词、网络密码或证书内容。";
  try(Reader reader=new InputStreamReader(new FileInputStream(file),"UTF-8")){char[] chars=new char[1024];StringBuilder text=new StringBuilder();int n;while((n=reader.read(chars))!=-1&&text.length()<24000)text.append(chars,0,n);return text.toString();}catch(IOException e){return "无法读取故障记录："+e.getClass().getSimpleName();}
 }
 public static void resumed(Activity a){install(a);}
 public static void show(Activity a){
  final String text=report(a);TextView log=new TextView(a);log.setText(text);log.setTextSize(12);log.setTextIsSelectable(true);ScrollView scroll=new ScrollView(a);scroll.addView(log);
  new AlertDialog.Builder(a).setTitle("仪表故障记录").setView(scroll).setPositiveButton("复制记录",(d,w)->{ClipboardManager manager=(ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE);manager.setPrimaryClip(ClipData.newPlainText("DiPlay 仪表故障",text));Toast.makeText(a,"已复制，可粘贴到聊天",Toast.LENGTH_LONG).show();}).setNeutralButton("暂停附加投屏",(d,w)->{
   for(String name:new String[]{"menglong_instrument_214","menglong_hud_214","menglong_app_video_214"})a.getSharedPreferences(name,0).edit().putBoolean("enabled",false).apply();
   guard(a,"关闭仪表",()->InstrumentOutput.destroyed(a));guard(a,"关闭 App 投屏",()->AppOutput.destroyed(a));Toast.makeText(a,"已暂停附加投屏，布局和颜色仍保留",Toast.LENGTH_LONG).show();
  }).setNegativeButton("关闭",null).show();
 }
}
