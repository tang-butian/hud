package com.shilapi.xcertplay.menglong;
import java.util.Locale;
/** Preserve telephony and existing Siri routes; retry generic MIC via the known voice path. */
public final class MicSourcePolicy {
 public static int select(int original,String type,boolean enabled){String t=type==null?"":type.toLowerCase(Locale.ROOT);return enabled&&original==1&&!t.equals("telephony")?6:original;}
 public static int peak(byte[] pcm,int count){if(pcm==null||count<=0)return 0;int result=0;for(int i=0;i+1<Math.min(count,pcm.length);i+=2){int sample=(short)((pcm[i]&255)|(pcm[i+1]<<8));result=Math.max(result,Math.abs(sample));}return result;}
}
