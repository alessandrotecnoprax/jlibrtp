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
package jlibrtp;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
/**
 * The purpose of this thread is to check whether there are packets ready from 
 * any participants.
 * 
 * It should sleep when not in use, and be woken up by a condition variable.
 * 
 * Optionally, if we do jitter-control, the condition variable should have a max waiting period 
 * equal to how often we need to push data.
 * 
 * @author Arne Kepp
 */
public class AppCallerThread extends Thread {
	RTPSession rtpSession;
	RTPAppIntf appl;
	
	public AppCallerThread(RTPSession session, RTPAppIntf rtpApp) {
		rtpSession = session;
		appl = rtpApp;
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> AppCallerThread created");
		}  
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 3) {
			System.out.println("-> AppCallerThread.run()");
		}
		
		while(rtpSession.endSession == false) {
			
			rtpSession.pktBufLock.lock();
		    try {
				if(RTPSession.rtpDebugLevel > 15) {
					System.out.println("<-> AppCallerThread going to Sleep");
				}
				
				try { rtpSession.pktBufDataReady.await(); } 
					catch (Exception e) { System.out.println("AppCallerThread:" + e.getMessage());}

		    	// Next loop over all participants and check whether they have anything for us.
				Iterator iter = rtpSession.partDb.getParticipants();
				
				while(iter.hasNext()) {
					Participant p = (Participant) iter.next();
					
					boolean done = false;
					while(!done && (p.rtpAddress != null || rtpSession.naiveReception) 
							&& p.pktBuffer != null && p.pktBuffer.length > 0) {
						
						DataFrame aFrame = p.pktBuffer.popOldestFrame();
						if(aFrame == null) {
							done = true;
						} else {
							appl.receiveData(aFrame.data,p, aFrame.timeStamp);
						}
					}
				}
		    
		     } finally {
		       rtpSession.pktBufLock.unlock();
		     }
			
		}
		if(RTPSession.rtpDebugLevel > 3) {
			System.out.println("<- AppCallerThread.run()");
		}  
	}

}
