package jlibrtp;
/**
 * Java RTP Library
 * Copyright (C) 2006 Arne Kepp
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
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
