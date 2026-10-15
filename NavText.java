package com.shilapi.xcertplay.menglong;
public final class NavText {
 public static String road(int code,String raw){if(raw==null||raw.trim().isEmpty())return HudFormat.direction(code);String s=raw.trim().replaceAll("\\s+"," ").replaceFirst("^(?:进入[ \u3000]*){2,}","进入 ");return s.startsWith("进入")||s.startsWith("驶入")||s.startsWith("沿")||s.startsWith("向")||s.startsWith("左转")||s.startsWith("右转")?s:"进入 "+s;}
}
