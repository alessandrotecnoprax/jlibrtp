package jlibrtp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Participant {
	
	String networkAdr;
	private int destPort = 32000;
	private boolean isSender = true;
	private boolean isReceiver = true;
	private InetAddress address = null;
	int ssrc = -1;
	public int lastSentSeqNumber;
	public int	lastRecvSeqNumber;
	long lastTimeStamp;
	private DatagramSocket socket;
	
	public Participant(String networkAddress,int port,String CNAME) {
		this.networkAdr = networkAddress;
		// Shouldnt we be getting this from them?
		this.ssrc = CNAME.hashCode();
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
	
	int getdestPort() {
		return destPort;
	}
	
	int getSSRC() {
		return this.ssrc;
	}
}
