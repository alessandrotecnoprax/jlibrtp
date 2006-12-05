package jlibrtp;
/*
 * The purpose of this thread is to check whether there are
 * packets ready from any participants.
 * 
 * It should sleep when not in use, and be woken up by a condition variable.
 * 
 * Optionally, if we do jitter-control, the condition variable should
 * have a max waiting period equal to how often we need to push data.
 */
import java.util.concurrent.locks.*;

public class AppCallerThread extends Thread {
	RTPSession session;
	RTPAppIntf appl;
	
	public AppCallerThread(RTPSession theSession, RTPAppIntf rtpApp) {
		session = theSession;
		appl = rtpApp;
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> AppCallerThread created");
		}  
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 3) {
			System.out.println("-> AppCallerThread.run()");
		}  
		while(session.endSession == false) {
			if(RTPSession.rtpDebugLevel > 15) {
				System.out.println("<-> AppCallerThread looooooop");
			}  
			
			// Wait until there is at least data available in one of the queues
		    // session.dataAvail.lock();
		    // try {
		    //	 
		    //	 while(moreData) {
		    //		 
		    //	 }
		    //} finally {
		    //     l.unlock();
		    // }
			String str = "abcdefg";
			appl.receiveData(str.getBytes(),"someone",40);
			try {
				Thread.sleep(100000);
			} catch(Exception e) {
				//Do nothing :)
			}
		}
		if(RTPSession.rtpDebugLevel > 3) {
			System.out.println("<- AppCallerThread.run()");
		}  
	}

}
