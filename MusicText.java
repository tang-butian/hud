package com.shilapi.xcertplay.menglong;
/** Keeps stable track metadata separate from phone-supplied current song/lyric text. */
public final class MusicText {
 public static final class Rows {
  public final String title,artist,lyrics;
  Rows(String title,String artist,String lyrics){this.title=title;this.artist=artist;this.lyrics=lyrics;}
 }
 private static String clean(String s){return s==null?"":s.replace("\r\n","\n").replace('\r','\n').trim();}
 public static Rows separate(String title,String artist,String phoneLine){
  title=clean(title);artist=clean(artist);String lyric=clean(phoneLine);
  if(!title.isEmpty()){
   String[] full={title,title+" - "+artist,title+" — "+artist,title+" – "+artist,title+" · "+artist};
   for(String known:full)if(lyric.equals(known)){lyric="";break;}
   if(lyric.startsWith(title+"\n"))lyric=lyric.substring(title.length()+1).trim();
  }
  if(!artist.isEmpty()&&lyric.endsWith("\n"+artist))lyric=lyric.substring(0,lyric.length()-artist.length()-1).trim();
  if(!artist.isEmpty()&&lyric.equals(artist))lyric="";
  return new Rows(title,artist,lyric);
 }
}
