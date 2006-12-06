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
				System.out.println("INSIDE RTCPRecvThread 1 the port="+this.rtcpPort);
				MulticastSocket s = new MulticastSocket(port);
		
				s.joinGroup(InetAddress.getByName(group));
				byte buf[] = new byte[1024];
				DatagramPacket pack = new DatagramPacket(buf, buf.length);
				s.receive(pack);
				System.out.println("INSIDE RTCPRecvThread 2");
				String ss = new String(buf);
				System.out.println("The data recvd="+ss);
				RTCPCommonHeader header = new RTCPCommonHeader(buf);
			
				if(header.getPktType() == 203) {
					System.out.println("Inside the if");
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
					System.out.println("The SDES pkt has been received");
					RTCPSDESHeader sdesPkt = new RTCPSDESHeader(buf);
					sdesPkt.decode();
					System.out.println("The CNAME rcvd is ="+sdesPkt.CNAME);
					System.out.println("The SSRC rcvd is ="+sdesPkt.ssrc);
				
					Participant P = (Participant) session.rtpSession.participantTable.get(new String(sdesPkt.CNAME));
					if(P != null && P.ssrc !=  -1)
					{
						System.out.println("The Newly selected Participant CNAME="+P.cname);
						//P.setSSRC(sdesPkt.ssrc);
						((Participant) session.rtpSession.participantTable.get(new String(sdesPkt.CNAME))).setSSRC(sdesPkt.ssrc);
						
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
