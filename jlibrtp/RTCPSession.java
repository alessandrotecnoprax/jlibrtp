package jlibrtp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RTCPSession {
	//tp: the last time an RTCP packet was transmitted;
	protected long tp;
	
	//tc: the current time, get from system

	//tn: the next scheduled transmission time of an RTCP packet, set up in a timer.

	// the estimated number of session members at the time tn
	 protected int pmembers = 0;

	// the most current estimate for number of members (get from partDb?)
	 //protected in members;
	 
	 // number of sender estimate (get from partDb?)
	 //protected int senders;

	//The target RTCP bandwidth, i.e., the total bandwidth that will be used for RTCP packets by all members of this session
	 protected int rtcp_bw = -1;
	 
	 //Flag that is true if the application has sent data since the 2nd previous RTCP report was transmitted.
	 protected boolean we_sent = false;
	      
	//The average compound RTCP packet size, in octets, including UDP and IP headers
	protected int avg_rtcp_size = 0;

	// Just starting up?
	protected boolean initial = true;

	// Parent session
	protected RTPSession rtpSession = null;
	protected boolean mcSession;
	 
	// Network stuff
	protected DatagramSocket rtcpSock = null;
	protected MulticastSocket rtcpMCSock = null;
	protected InetAddress mcGroup = null;
	
	// Threads
	protected RTCPReceiverThread recvThrd = null;
	protected RTCPSenderThread senderThrd = null;
	
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
	
	protected void start() {
		recvThrd = new RTCPReceiverThread(this, this. rtpSession);
		senderThrd = new RTCPSenderThread(this , this.rtpSession);
	}
	
}
