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
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

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
			
			session.pktBufLock.lock();
		    try {
				if(RTPSession.rtpDebugLevel > 15) {
					System.out.println("<-> AppCallerThread going to Sleep");
				}
		    	// We can add timeout to this
		    	try { session.pktBufDataReady.await(5, TimeUnit.MILLISECONDS); } catch (Exception e) { System.out.println("AppCallerThread:" + e.getMessage());} 
				if(RTPSession.rtpDebugLevel > 15) {
					System.out.println("<-> AppCallerThread waking up");
				}
		    	// Next loop over all participants and check whether they have anything for us.
				Enumeration set = session.participantTable.elements();
				while(set.hasMoreElements()) {
					Participant p = (Participant)set.nextElement();
					
					while(p.isSender() && p.pktBuffer != null && p.pktBuffer.length > 5 && p.pktBuffer.frameIsReady()) {
						DataFrame aFrame = p.pktBuffer.popOldestFrame();
						appl.receiveData(aFrame.data,p.getCNAME(),aFrame.timeStamp);
					}
				}
		    
		     } finally {
		       session.pktBufLock.unlock();
		     }
			
		}
		if(RTPSession.rtpDebugLevel > 3) {
			System.out.println("<- AppCallerThread.run()");
		}  
	}

}
