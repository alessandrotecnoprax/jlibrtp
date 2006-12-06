package jlibrtp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RTCPRecvrThread extends Thread   {
	
	RTCPSession session = null;
	int rtcpPort = 0;
	
	RTCPRecvrThread(int rtcpPort,RTCPSession session) {
		this.rtcpPort = rtcpPort;
		this.session = session;
	}
	
	public void run() {
		while(!this.session.rtpSession.endSession) {
			int port = this.rtcpPort;
			String group = "225.4.5.6";
			try
			{
				if(RTPSession.rtpDebugLevel > 1){
				System.out.println("INSIDE RTCPRecvThread 1 the port="+this.rtcpPort);
				}
				MulticastSocket s = new MulticastSocket(port);
		
				s.joinGroup(InetAddress.getByName(group));
				byte buf[] = new byte[1024];
				DatagramPacket pack = new DatagramPacket(buf, buf.length);
				s.receive(pack);
				
				String ss = new String(buf);
				
				RTCPCommonHeader header = new RTCPCommonHeader(buf);
			
				if(header.getPktType() == 203) {
					
					this.session.rtpSession.endSession = true;
				}
				else if(header.getPktType() == 201)
				{
					System.out.println("The RR Packet received");
					RTCPRRPkt rrPkt = new RTCPRRPkt();
					rrPkt.decodeRRPkt(buf);
					
				}
				else if(header.getPktType() == 202)
				{
					
					RTCPSDESHeader sdesPkt = new RTCPSDESHeader(buf);
					sdesPkt.decode();
					//if(RTPSession.rtpDebugLevel > 1){
						System.out.println("The SDES pkt has been received");
					System.out.println("The CNAME rcvd is ="+sdesPkt.CNAME);
					System.out.println("The SSRC rcvd is ="+sdesPkt.ssrc);
					//}
				
			//		Participant P = (Participant) session.rtpSession.participantTable.get(new String(sdesPkt.CNAME));
					Participant P = (Participant) session.rtpSession.participantTable.get(new Long(sdesPkt.ssrc));
					if(P != null && P.ssrc !=  -1)
					{
						System.out.println("The Newly selected Participant CNAME="+sdesPkt.ssrc);
						//P.setSSRC(sdesPkt.ssrc);
					//	((Participant) session.rtpSession.participantTable.get(new String(sdesPkt.CNAME))).setSSRC(sdesPkt.ssrc);
						((Participant) session.rtpSession.participantTable.get(new Long(sdesPkt.ssrc))).setSSRC(sdesPkt.ssrc);
						((Participant) session.rtpSession.participantTable.get(new Long(sdesPkt.ssrc))).cname = sdesPkt.CNAME;
						
					}
					
					
					
				}
				s.leaveGroup(InetAddress.getByName(group));
				s.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

	}
}
