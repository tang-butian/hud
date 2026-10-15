package com.shilapi.xcertplay.menglong;
import com.shilapi.xcertplay.menglong.LayoutGeometry.Box;
/** Explicit toolbar selection; one gesture owns one rectangle until it ends. */
public final class EditSelection {
 private final int count;private int selected,active=-1;
 public EditSelection(int count){if(count<1)throw new IllegalArgumentException();this.count=count;}
 public int selected(){return selected;}
 public int target(){return active;}
 public void select(int index){if(index<0||index>=count)throw new IllegalArgumentException();selected=index;}
 public boolean begin(Box[] boxes,float x,float y,float marginX,float marginY){
  active=-1;if(boxes.length!=count)throw new IllegalArgumentException();Box b=boxes[selected];
  if(x<b.l-marginX||x>b.r+marginX||y<b.t-marginY||y>b.b+marginY)return false;
  active=selected;return true;
 }
 public void end(){active=-1;}
}
