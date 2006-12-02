package jlibrtp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;

public class RTPSenderThread extends Thread
{
	RTPSession session = null;
	RTPSenderThread(RTPSession session)
	{
		this.session = session;
	}
	public void run()
	{
	 Hashtable participantTable = session.getParticipantDB();
	 RtpPkt pkt = null;
		while(true)
		{
			pkt = session.getFrameToSend();
			while( pkt != null)
			{
				Enumeration set = participantTable.elements();

				while(set.hasMoreElements())
				{
					Participant p = (Participant)set.nextElement();
					
					if(p.isSender())
					{
						try
						{
							int i=0;
							if(RTPSession.rtpDebugLevel > 4) {
								System.out.println("RTPSenderThread: pkt.encode().length  ="+pkt.encode().length + " The port="+p.getdestPort());
							}
							DatagramPacket packet = new DatagramPacket(pkt.encode(),pkt.encode().length , InetAddress.getByName(p.sendingHost), p.getdestPort());
							p.getSocket().send(packet);
						}
						catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					p = null;
				}
				pkt = null;
			}
			
		}
	}
	
	  
}
