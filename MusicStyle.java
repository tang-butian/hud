package com.shilapi.xcertplay.menglong;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.widget.*;
import java.util.Locale;

/** Shared color settings for the instrument music region and HUD music region. */
public final class MusicStyle {
 public static final int DEFAULT_TITLE=0xff253443,DEFAULT_LYRIC=0xff00684d;
 private static SharedPreferences prefs(Context c){return c.getSharedPreferences("menglong_music_style_214",0);}
 public static int title(Context c){return MusicPalette.title(prefs(c).getInt("title_color",DEFAULT_TITLE)|0xff000000,NightTheme.isNight());}
 public static int lyric(Context c){return prefs(c).getInt("lyric_color",DEFAULT_LYRIC)|0xff000000;}
 public static int hudTitle(Context c){return prefs(c).getInt("title_color",Color.WHITE)|0xff000000;}
 public static int hudLyric(Context c){return c.getSharedPreferences("menglong_hud_214",0).getInt("lyric_color",0xff53e0bc)|0xff000000;}
 private static String hex(int color){return String.format(Locale.US,"#%06X",color&0xffffff);}
 private static TextView label(Context c,String s){TextView t=new TextView(c);t.setText(s);t.setTextSize(15);return t;}
 private static void preview(TextView t,int color){t.setTextColor(color);t.setShadowLayer(0,0,0,Color.TRANSPARENT);}
 public static void hudSettings(Activity a){picker(a,"HUD 歌词颜色",hudLyric(a),value->{a.getSharedPreferences("menglong_hud_214",0).edit().putInt("lyric_color",value).apply();Toast.makeText(a,"HUD 歌词颜色已保存",Toast.LENGTH_SHORT).show();});}
 public static void settings(Activity a){
  int[] colors={prefs(a).getInt("title_color",DEFAULT_TITLE)|0xff000000,lyric(a)};LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);int pad=Math.round(16*a.getResources().getDisplayMetrics().density);body.setPadding(pad,pad,pad,pad);
  body.addView(label(a,"歌名与歌词各占独立行，可分别设置颜色。歌手跟随歌名颜色；默认歌名配色在夜间自动变浅，自选颜色保留。"));
  LinearLayout day=new LinearLayout(a);day.setOrientation(LinearLayout.VERTICAL);day.setPadding(pad,pad,pad,pad);day.setBackgroundColor(Color.WHITE);TextView dayTitle=label(a,"歌曲名称 · 歌手"),dayLyric=label(a,"当前歌词显示在下一行");day.addView(dayTitle);day.addView(dayLyric);body.addView(day);
  LinearLayout night=new LinearLayout(a);night.setOrientation(LinearLayout.VERTICAL);night.setPadding(pad,pad,pad,pad);night.setBackgroundColor(0xff0a1018);TextView nightTitle=label(a,"歌曲名称 · 歌手"),nightLyric=label(a,"当前歌词显示在下一行");night.addView(nightTitle);night.addView(nightLyric);body.addView(night);
  Button title=new Button(a),lyric=new Button(a);body.addView(title);body.addView(lyric);
  Runnable refresh=()->{title.setText("歌名颜色 "+hex(colors[0]));lyric.setText("歌词颜色 "+hex(colors[1]));preview(dayTitle,colors[0]);preview(nightTitle,MusicPalette.title(colors[0],true));preview(dayLyric,colors[1]);preview(nightLyric,colors[1]);};refresh.run();
  title.setOnClickListener(v->picker(a,"选择歌名颜色",colors[0],value->{colors[0]=value;refresh.run();}));lyric.setOnClickListener(v->picker(a,"选择歌词颜色",colors[1],value->{colors[1]=value;refresh.run();}));
  Button reset=new Button(a);reset.setText("浅色仪表配色：深灰歌名 + 深绿歌词");reset.setOnClickListener(v->{colors[0]=DEFAULT_TITLE;colors[1]=DEFAULT_LYRIC;refresh.run();});body.addView(reset);
  ScrollView scroll=new ScrollView(a);scroll.addView(body);new AlertDialog.Builder(a).setTitle("歌名与歌词颜色").setView(scroll).setPositiveButton("保存颜色",(d,w)->{prefs(a).edit().putInt("title_color",colors[0]).putInt("lyric_color",colors[1]).apply();Toast.makeText(a,"颜色已保存，仪表与 HUD 音乐区自动更新",Toast.LENGTH_SHORT).show();}).setNegativeButton("取消",null).show();
 }
 private interface Chosen {void color(int value);}
 private static void picker(Activity a,String title,int initial,Chosen chosen){
  LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);int pad=Math.round(12*a.getResources().getDisplayMetrics().density);body.setPadding(pad,pad,pad,pad);TextView sample=label(a,"颜色预览");sample.setTextSize(22);sample.setBackgroundColor(0xffdfe8e7);sample.setPadding(pad,pad,pad,pad);body.addView(sample);TextView valueLabel=label(a,"");body.addView(valueLabel);
  SeekBar[] bars={new SeekBar(a),new SeekBar(a),new SeekBar(a)};TextView[] values={label(a,""),label(a,""),label(a,"")};String[] names={"红","绿","蓝"};
  Runnable refresh=()->{int color=Color.rgb(bars[0].getProgress(),bars[1].getProgress(),bars[2].getProgress());sample.setTextColor(color);valueLabel.setText(hex(color));for(int i=0;i<3;i++)values[i].setText(names[i]+" "+bars[i].getProgress());};
  for(int i=0;i<3;i++){bars[i].setMax(255);bars[i].setProgress((initial>>(16-i*8))&255);body.addView(values[i]);body.addView(bars[i]);bars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){refresh.run();}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});}
  int[] presets={Color.BLACK,Color.WHITE,0xff00a86b,0xff00b9d9,0xffffd43b,0xffff7d35,0xffe63850,0xff3279e6};String[] labels={"黑","白","绿","青","黄","橙","红","蓝"};
  for(int row=0;row<2;row++){LinearLayout line=new LinearLayout(a);for(int col=0;col<4;col++){int index=row*4+col;Button b=new Button(a);b.setText(labels[index]);line.addView(b,new LinearLayout.LayoutParams(0,-2,1));b.setOnClickListener(v->{int color=presets[index];for(int i=0;i<3;i++)bars[i].setProgress((color>>(16-i*8))&255);refresh.run();});}body.addView(line);}refresh.run();ScrollView scroll=new ScrollView(a);scroll.addView(body);
  new AlertDialog.Builder(a).setTitle(title).setView(scroll).setPositiveButton("使用此颜色",(d,w)->chosen.color(Color.rgb(bars[0].getProgress(),bars[1].getProgress(),bars[2].getProgress()))).setNegativeButton("取消",null).show();
 }
}
