package jlibrtp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Participant {
	private int destPort = 32000;
	private boolean isSender = true;
	private boolean isReceiver = true;
	private InetAddress address = null;
	long ssrc = -1;
	String cname;
	public int lastSentSeqNumber;
	public int	lastRecvSeqNumber;
	long lastTimeStamp;
	boolean unknown = true;
	//Store the packets received from this participant
	public PktBuffer pktBuffer;
	
	// Known contact, but we don't know their ssrc yet.
	public Participant(String networkAddress,int port,String CNAME) {
		if(RTPSession.rtpDebugLevel > 6) {
			System.out.println("New participant created: " + CNAME + "@" + networkAddress);
		}
		// Shouldnt we be getting this from them?
		this.unknown = false;
		try {
			address = InetAddress.getByName(networkAddress);
		} catch (Exception e) {
			System.out.println("Couldn't resolve " + networkAddress);
		}
		cname = CNAME;

	}
	// Incomplete insert, we got a packet, but we don't know this person yet.
	public Participant(InetAddress adr, int port, long SSRC) {
		address = adr;
		destPort = port;
		ssrc = SSRC;
	}
	
	public void isSender(boolean doesSend) {
		isSender = doesSend;
	}

	public boolean isSender() {
		return isSender;
	}
	
	public void isReceiver(boolean doesReceive) {
		isReceiver = doesReceive;
	}

	public boolean isReceiver() {
		return isReceiver;
	}
	
	int getDestPort() {
		return destPort;
	}
	
	InetAddress getInetAddress() {
		return address;
	}
	String getCNAME() {
		return cname;
	};
	
	
	long getSSRC() {
		return this.ssrc;
	}
	
	int setSSRC(long anSSRC) {
		if(ssrc > 0) {
			return -1;
		} else {
			ssrc = anSSRC;
			return 0;
		}
	}
	
}
