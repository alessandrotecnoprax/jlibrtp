package jlibrtp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RTCPSession {
	//The target RTCP bandwidth, i.e., the total bandwidth that will be used for RTCP packets by all members of this session
	//protected int rtcp_bw = -1;
	
	//Flag that is true if the application has sent data since the 2nd previous RTCP report was transmitted.
	protected boolean we_sent = false;

	protected int nextDelay = -1; //Delay between RTCP transmissions, in ms. Initialized in start()
	protected int avgPktSize = 200; //The average compound RTCP packet size, in octets, including UDP and IP headers

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
		nextDelay = 2500 + rtpSession.random.nextInt(1000) - 500;
		recvThrd = new RTCPReceiverThread(this, this. rtpSession);
		senderThrd = new RTCPSenderThread(this , this.rtpSession);
	}

	protected void sendByes() {
		senderThrd.sendByes();
	}

	/**
	 * Update the average packet size
	 * @param length of latest packet
	 */
	synchronized protected void calculateDelay() {
		int rand = rtpSession.random.nextInt(1000) - 500; //between -500 and +500

		if(rtpSession.bandwidth != 0) {
			// This does not distinguish between senders and receivers, yet.
			double numerator = ((double) this.avgPktSize)*((double) rtpSession.partDb.size());
			double denominator = 0.05 * rtpSession.bandwidth;
			this.nextDelay = (int) Math.round(1000.0*(numerator/denominator)) + rand;
		} else {
			if(this.initial) {
				// 1.5 to 2.5 seconds, randomly
				this.nextDelay = 2000 + rand;
			} else {
				// 4.5 to 5.5 seconds, randomly
				this.nextDelay = 5000 + rand;
			}

		}
		if(this.nextDelay < 2000) {
			System.out.println("RTCPSession.calculateDelay() nextDelay was too short (" 
					+this.nextDelay+"ms), setting to "+(this.nextDelay = 2000 + rand));
		}
	}

	/**
	 * Update the average packet size
	 * @param length of latest packet
	 */
	synchronized protected void updateAvgPacket(int length) {
		double tempAvg = (double) this.avgPktSize;
		tempAvg = (15*tempAvg + ((double) length))/16;
		this.avgPktSize = (int) tempAvg;
	}
}
