package jlibrtp;

import java.util.concurrent.locks.*;

public class AppCallerThread extends Thread {
	RTPSession session;
	RTPAppIntf appl;
	
	public AppCallerThread(RTPSession theSession, RTPAppIntf rtpApp, Lock dataAvail, PartDB) {
		session = theSession;
		appl = rtpApp;
		
		while(session.endSession != false) {
			// Wait until there is at least data available in one of the queues
		     dataAvail.lock();
		     try {
		    	 
		    	 while(moreData) {
		    		 
		    	 }
		     } finally {
		         l.unlock();
		     }
		}
	}
	
	public void run() {
		
	}

}
