package com.shilapi.xcertplay.menglong;
/** Isolates an optional display operation while allowing fatal VM errors to propagate. */
public final class UiGuard {
 public interface Report {void failed(Throwable error);}
 public static boolean run(Runnable operation,Report report){
  try{operation.run();return true;}
  catch(RuntimeException|LinkageError error){try{report.failed(error);}catch(RuntimeException|LinkageError ignored){}return false;}
 }
}
