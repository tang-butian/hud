package com.shilapi.xcertplay.menglong;
import android.media.AudioTrack;
/** Compatibility for existing audio hooks. The cancelled volume feature does nothing. */
public final class NavigationVolume {
 private NavigationVolume(){}
 public static void track(AudioTrack track,Object channel){}
 public static void released(AudioTrack track){}
}
