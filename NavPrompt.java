package com.shilapi.xcertplay.menglong;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.shilapi.xcertplay.menglong.LayoutGeometry.Box;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;
import com.shilapi.xcertplay.glance.CarPlayGlance;
import com.shilapi.xcertplay.hud.BydManeuverCodes;
/** Maneuver data from the independent CarPlay metadata channel. No screen capture. */
public final class NavPrompt {
 public static void foreground(Activity a){}
 public static void paused(Activity a){}
 public static final class Overlay extends View {
  private final int display;private boolean active,live=true;private int code;private String distance="",road="";
  public Overlay(Context c,Activity a,int display){super(c);this.display=display;setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
  public void stop(){live=false;active=false;}
  public void update(){if(!live)return;CarPlayGlance.Snapshot s=CarPlayGlance.INSTANCE.snapshot();active=s!=null&&s.getConnected()&&s.getManeuverType()!=null;if(active){code=BydManeuverCodes.INSTANCE.gaode(s.getManeuverType(),s.getDrivingSide());distance=HudFormat.distance(s.getDistanceMeters());road=s.getRoad()==null?"":s.getRoad();}invalidate();}
  @Override protected void onDraw(Canvas c){super.onDraw(c);if(active){SharedPreferences p=getContext().getSharedPreferences("menglong_instrument_214",0);String k="d"+display+"_prompt_";Box b=LayoutGeometry.valid(new Box(p.getFloat(k+"l",.10f),p.getFloat(k+"t",.15f),p.getFloat(k+"r",.37f),p.getFloat(k+"b",.42f)));GaodeCard.draw(c,new RectF(b.l*getWidth(),b.t*getHeight(),b.r*getWidth(),b.b*getHeight()),code,distance,road);}}
 }
}
