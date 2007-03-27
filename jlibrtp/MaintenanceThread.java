package jlibrtp;

import java.util.*;

/**
 * The purpose of this thread is to wake up every X seconds and
 * clean up the database, primarily to throw out participants
 * we have not heard from in a while.
 * 
 * @author Arne Kepp
 */
public class MaintenanceThread extends Thread {
	RTPSession rtpSession = null;
	long lastRun;
	boolean rollOver = false;
	
	protected MaintenanceThread(RTPSession rtpSession) {
		this.rtpSession = rtpSession;
		this.lastRun = System.currentTimeMillis(); 
	}
	
	public void run() {
		while(!rtpSession.endSession && rtpSession.maintenanceInterval > 0) {
			
			try { Thread.sleep(rtpSession.maintenanceInterval); } catch(Exception e) {
				System.out.println("MaintenanceThread in trouble");
			}

			long curTime = System.currentTimeMillis();
			
			//If the timer rolls over we'll just skip this interval
			if(curTime < this.lastRun) {
				this.rollOver = true;
				continue;
			}
			
			Iterator iter = rtpSession.partDb.getParticipants();
			
			while(iter.hasNext()) {
				Participant part = (Participant) iter.next();
				
				if(part.persistent && part.timestampBYE > this.lastRun) {
					continue;
				}
				
				if(this.lastRun > part.addedByApp && this.lastRun > part.lastRtpPkt 
						&& this.lastRun > part.lastRtcpRRPkt) {
					
					//This one hasn't been heard from in a long while.
					if(RTPSession.rtpDebugLevel > 1)
						System.out.println("<-> MaintenanceThread removing "+part.rtcpAddress+" "+part.ssrc);

					rtpSession.partDb.removeParticipant(part);
					
				} else if(this.rollOver) {
					//Now the tables are turned, smaller means larger
					if( 	((part.lastRtpPkt > 0 && part.lastRtpPkt > lastRun)
							&& (part.lastRtcpPkt > 0 && part.lastRtcpPkt > lastRun))
							|| part.addedByApp < lastRun) {
						
						if(RTPSession.rtpDebugLevel > 1)
							System.out.println("<-> MaintenanceThread removing "+part.rtcpAddress+" "+part.ssrc);
						
						rtpSession.partDb.removeParticipant(part);
						
					} else if( part.addedByApp > lastRun && part.addedByApp < curTime) {
						//Very special case, make it easier to deal with
						part.addedByApp = curTime -1;
					}
					this.rollOver = false;
				}
			}
			
			this.lastRun = curTime;
			
		}
		
	}
}
