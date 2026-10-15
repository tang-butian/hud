package com.shilapi.xcertplay.menglong;
import android.app.*;
import android.content.*;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import com.shilapi.xcertplay.orchestration.*;
import java.util.ArrayDeque;
import java.util.Locale;

/** A reversible wireless-only focus workaround plus local event diagnostics. */
public final class AudioCompatibility {
 private static final ArrayDeque<String> events=new ArrayDeque<String>();
 private static final long origin=SystemClock.elapsedRealtime();private static Boolean lastPlaying;
 private static SharedPreferences prefs(Context c){return c.getSharedPreferences("menglong_wireless_audio_214",0);}
 public static boolean requestFocus(boolean wireless,boolean compatibility){return !wireless||!compatibility;}
 private static synchronized void add(String line){if(events.size()>=160)events.removeFirst();events.addLast(String.format(Locale.CHINA,"+%.1fs ", (SystemClock.elapsedRealtime()-origin)/1000.0)+line);}
 public static boolean allowFocus(Context context,CarPlayController controller){
  try{boolean wireless=controller!=null&&CarPlayController.access$getConfig$p(controller).getTransport()==CarPlayTransport.WIRELESS;boolean mode=prefs(context).getBoolean("compatibility",false);boolean allowed=requestFocus(wireless,mode);add("启动媒体会话："+(wireless?"无线":"USB / 有线")+"，兼容="+mode+"，额外焦点申请="+allowed);return allowed;}
  catch(RuntimeException e){add("连接类型读取失败，保留原流程："+e.getClass().getSimpleName());return true;}
 }
 public static void focus(int change){add("系统焦点变化="+change);}
 public static void command(String kind){add("收到系统媒体控制："+kind);}
 public static void key(KeyEvent event){if(event!=null)add("收到媒体按键："+KeyEvent.keyCodeToString(event.getKeyCode())+"，设备="+event.getDeviceId()+"，action="+event.getAction()+"，repeat="+event.getRepeatCount());}
 public static synchronized void playing(boolean value){if(lastPlaying==null||lastPlaying!=value){lastPlaying=value;add("手机播放状态="+(value?"播放":"暂停"));}}
 private static synchronized String snapshot(Context c){StringBuilder s=new StringBuilder("无线音频诊断\n兼容模式：").append(prefs(c).getBoolean("compatibility",false)).append("\n原版音频焦点开关：").append(c.getSharedPreferences("xcertplay_airplay",0).getBoolean("audio_focus_enabled",false)).append("\n事件仅保存在本次运行内存中，不包含歌名、蓝牙名称或网络密码。\n\n");for(String line:events)s.append(line).append('\n');return s.toString();}
 public static void resumed(Activity a){}
 public static void settings(Activity a){LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);int pad=Math.round(16*a.getResources().getDisplayMetrics().density);body.setPadding(pad,pad,pad,pad);CheckBox mode=new CheckBox(a);mode.setText("猛龙无线音频兼容模式");mode.setChecked(prefs(a).getBoolean("compatibility",false));body.addView(mode);TextView info=new TextView(a);info.setText("0.2.18 默认关闭此兼容选项，按原版流程播放；出现无线暂停时才尝试开启。开启后只在无线连接时跳过媒体按键模块额外申请的音频焦点。不会自动播放或忽略暂停命令；USB 保留原流程。更改后重新连接生效。\n如果仍暂停，请复制诊断信息，查看系统媒体命令和手机状态。");body.addView(info);
  new AlertDialog.Builder(a).setTitle("无线音频兼容与诊断").setView(body).setPositiveButton("保存",(d,w)->{prefs(a).edit().putBoolean("compatibility",mode.isChecked()).apply();Toast.makeText(a,"已保存，请断开并重新连接 CarPlay",Toast.LENGTH_LONG).show();}).setNeutralButton("查看诊断",(d,w)->showLogs(a)).setNegativeButton("取消",null).show();}
 private static void showLogs(Activity a){final String report=snapshot(a);TextView t=new TextView(a);t.setText(report);t.setTextSize(12);t.setTextIsSelectable(true);ScrollView scroll=new ScrollView(a);scroll.addView(t);new AlertDialog.Builder(a).setTitle("无线音频诊断").setView(scroll).setPositiveButton("复制诊断",(d,w)->{ClipboardManager clipboard=(ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE);clipboard.setPrimaryClip(ClipData.newPlainText("DiPlay 音频诊断",report));Toast.makeText(a,"诊断已复制，可粘贴到聊天",Toast.LENGTH_SHORT).show();}).setNeutralButton("清空",(d,w)->{synchronized(AudioCompatibility.class){events.clear();lastPlaying=null;}}).setNegativeButton("关闭",null).show();}
}
