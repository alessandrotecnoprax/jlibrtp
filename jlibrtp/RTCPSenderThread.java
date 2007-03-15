package jlibrtp;

import java.util.concurrent.TimeUnit;

public class RTCPSenderThread extends Thread {
	private RTPSession rtpSession = null;
	private RTCPSession rtcpSession = null;
	
	protected RTCPSenderThread(RTCPSession rtcpSession, RTPSession rtpSession) {
		this.rtpSession = rtpSession;
		this.rtcpSession = rtcpSession;
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTCPSenderThread created");
		} 
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<-> RTCPSenderThread running");
		}
		
		while {
			try { rtpSession.pktBufDataReady.await(rtcpSession.nextDelay, TimeUnit.MILLISECONDS); } 
			catch (Exception e) { System.out.println("AppCallerThread:" + e.getMessage());}
		
			
		}
		
	}
}
