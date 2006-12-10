package jlibrtp;
/**
 * Java RTP Library
 * Copyright (C) 2006 Vaishnav Janardhan / Arne Kepp
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




import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Participant {
	private int destPort = 32000;
	private boolean isSender = true;
	private boolean isReceiver = true;
	private InetAddress address = null;
	long ssrc = -1;
	public String cname;
	public int lastSentSeqNumber;
	public int	lastRecvSeqNumber;
	long lastTimeStamp;
	boolean unknown = true;
	//Store the packets received from this participant
	public PktBuffer pktBuffer;
/*	Participant()
	{
		
	}*/
	// Known contact, but we don't know their ssrc yet.
	public Participant(String networkAddress,int aDestPort,String CNAME) {
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
		destPort = aDestPort;
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
	
	public String getCNAME() {
		return cname;
	}
	
	
	public long getSSRC() {
		return this.ssrc;
	}
	
	public int setSSRC(long anSSRC) {
		if(anSSRC < 0) {
			return -1;
		} else {
			ssrc = anSSRC;
			return 0;
		}
	}
	
}
