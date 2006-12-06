package jlibrtp;

public class RTCPSession implements Signalable
{
	int rtcpPort = 0;
	RTPSession rtpSession = null;
	RTCPBYESenderThread byeThread = null;
	RTCPRecvrThread recvThread = null;
	RTCPRRSendThread rrSendThread = null;
	RTCPSDESHeader sdesThread = null;
	RTCPSession(int rtcpPort,RTPSession rtpSession) {
		this.rtcpPort = rtcpPort;
		this.rtpSession = rtpSession;
		byeThread = new RTCPBYESenderThread(rtcpPort,this);
		this.recvThread = new RTCPRecvrThread(rtcpPort,this);
		
		this.rrSendThread = new RTCPRRSendThread(rtcpPort,this);
		this.sdesThread = new RTCPSDESHeader(rtcpPort,this);
		
		this.recvThread.start();
		
		
	}
	
	RTPSession getRTPSession()
	{
		return this.rtpSession;
	}
	
	void requestBYE(int ssrc)
	{
		System.out.println("BYE Request rcvd");
		long [] ssrcArray = new long[1];
		ssrcArray[0] = ssrc;
		
		RTCPByePkt byePkt = new RTCPByePkt(1,ssrcArray);
		// Just to avoid compile complaints
		//byePkt.setSSRCCount(this.rtpSession.ssrc);
		
		byeThread.sendBYEMsg(byePkt);
	}
	
	public void signalTimeout() {
		// TODO Auto-generated method stub
		
	}

}
