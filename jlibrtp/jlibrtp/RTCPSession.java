package jlibrtp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RTCPSession extends Thread {
	// Parent session
	protected RTPSession rtpSession = null;
	protected boolean mcSession;
	 
	// Network stuff
	protected DatagramSocket rtcpSock = null;
	protected MulticastSocket rtcpMCSock = null;
	protected InetAddress mcGroup = null;
	
	// Treads
	protected RTPReceiverThread recvThrd = null;
	
	protected RTCPSession(RTPSession parent, DatagramSocket rtcpSocket) {
		mcSession = false;
		rtcpSock = rtcpSocket;
		rtpSession = parent;
	}
	
	protected RTCPSession(RTPSession parent, MulticastSocket rtcpSocket, InetAddress multicastGroup) {
		mcSession = true;
		mcGroup = multicastGroup;
		rtcpSock = rtcpSocket;
		rtpSession = parent;
	}
	
}
