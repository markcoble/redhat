package org.coble.core.camel.test;

public class Wait{
	  public static void For(int timeoutInSeconds, int intervalInSeconds, ToHappen toHappen) {
	    long start=System.currentTimeMillis();
	    long end=start+(timeoutInSeconds*1000);
	    boolean timeout=false;
	    while(!toHappen.hasHappened() && !timeout){
	      try{
	        Thread.sleep((intervalInSeconds*1000));
	      }catch(InterruptedException ignor){}
	      timeout=System.currentTimeMillis()>end;
	      if (timeout) System.out.println("timed out waiting.");
	    }
	  }
	  public static void For(int timeoutInSeconds, ToHappen toHappen) {
	    For(timeoutInSeconds, 1, toHappen);
	  }
	}