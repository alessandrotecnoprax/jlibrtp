package jlibrtp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Enumeration;


public class RTCPSRSendThread implements Signalable
{
	RTPSession rtpSession = null;

	RTCPSenderReport sendReport = null;
	RTCPSRSendThread(RTPSession rtpSession)
	{
		this.rtpSession = rtpSession;
	
		this.sendReport = new RTCPSenderReport(this.rtpSession.ssrc,this.rtpSession);
		Timer t = new Timer(8000,this);
		t.startTimer();
	}
	
	
	public void signalTimeout() 
	{
	      

	      //String group = "225.4.5.6";
  		Enumeration set = this.rtpSession.partDb.getReceivers();
		
		while(set.hasMoreElements()) {
			Participant p = (Participant)set.nextElement();

			if(p.isReceiver()) {
				byte[] rawSRPkt = this.sendReport.encodeSRPkt();
				try
				{
			      

			 
					DatagramPacket pack = new DatagramPacket(rawSRPkt, rawSRPkt.length,
							p.getInetAddress(), p.getRtcpDestPort());
						
			      //     s.send(pack);
			      //     s.close();
			      if(this.rtpSession.mcSession == false)
			      {
			    	  this.rtpSession.rtpSock.send(pack);
			      }
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
			Timer t = new Timer(8000,this);
			t.startTimer();
	

	}
}
