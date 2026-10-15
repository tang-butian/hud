package com.shilapi.xcertplay.menglong;
import java.util.Locale;
public final class HudFormat {
 public static String distance(long meters){if(meters<0)return "";return meters<1000?meters+" 米":String.format(Locale.CHINA,"%.1f 公里",meters/1000.0);}
 public static String duration(Long seconds){if(seconds==null||seconds<0)return "";long m=(seconds+59)/60;return m<60?m+" 分钟":m/60+" 小时 "+m%60+" 分钟";}
 public static String direction(int code){switch(code){case 1:return "左转";case 2:return "右转";case 3:return "向左前方";case 4:return "向右前方";case 7:return "向左急转";case 8:return "向右急转";case 9:case 10:return "掉头";case 11:return "直行";case 13:return "进入环岛";case 48:return "到达目的地";default:if(code>=25&&code<=34)return "环岛第 "+(code-24)+" 出口";if(code>=35&&code<=44)return "环岛第 "+(code-34)+" 出口";return "导航指引";}}
 public static String arrow(int code){switch(code){case 1:case 7:return "←";case 2:case 8:return "→";case 3:return "↖";case 4:return "↗";case 9:case 10:return "↶";case 11:return "↑";case 48:return "●";default:return code==13||(code>=25&&code<=44)?"↻":"◇";}}
}
