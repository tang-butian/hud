package com.shilapi.xcertplay.menglong;
/** Only adapt the built-in deep-gray default; keep the selected custom colors. */
public final class MusicPalette {
 public static int title(int chosen,boolean night){return night&&chosen==0xff253443?0xffe6edf5:chosen;}
 public static String first(String primary,String fallback){return primary!=null&&!primary.trim().isEmpty()?primary:fallback;}
}
