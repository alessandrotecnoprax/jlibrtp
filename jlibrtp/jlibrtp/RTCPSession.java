package jlibrtp;

import java.util.Enumeration;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RTCPSession {
	protected long prevTime = System.currentTimeMillis();
	protected int nextDelay = -1; //Delay between RTCP transmissions, in ms. Initialized in start()
	protected int avgPktSize = 200; //The average compound RTCP packet size, in octets, including UDP and IP headers

	// Just starting up?
	protected boolean initial = true;

	// Parent session
	protected RTPSession rtpSession = null;
	
	// Network stuff
	protected DatagramSocket rtcpSock = null;
	protected MulticastSocket rtcpMCSock = null;
	protected InetAddress mcGroup = null;

	// Threads
	protected RTCPReceiverThread recvThrd = null;
	protected RTCPSenderThread senderThrd = null;

	protected RTCPSession(RTPSession parent, DatagramSocket rtcpSocket) {
		this.rtcpSock = rtcpSocket;
		rtpSession = parent;
	}

	protected RTCPSession(RTPSession parent, MulticastSocket rtcpSocket, InetAddress multicastGroup) {
		mcGroup = multicastGroup;
		this.rtcpSock = rtcpSocket;
		rtpSession = parent;
	}

	protected void start() {
		nextDelay = 2500 + rtpSession.random.nextInt(1000) - 500;
		recvThrd = new RTCPReceiverThread(this, this.rtpSession);
		senderThrd = new RTCPSenderThread(this, this.rtpSession);
		recvThrd.start();
		senderThrd.start();
	}

	protected void sendByes() {
		senderThrd.sendByes();
	}

	/**
	 * Update the average packet size
	 * @param length of latest packet
	 */
	synchronized protected void calculateDelay() {
		
		long curTime = System.currentTimeMillis();
		

		
		if(rtpSession.bandwidth != 0 && ! this.initial && rtpSession.partDb.ssrcTable.size() > 4) {
			// RTPs mechanisms for RTCP scalability
			int rand = rtpSession.random.nextInt(10000) - 5000; //between -500 and +500
			double randDouble = ((double) rand)/1000.0;
			
			int senderCount = 0;
			Enumeration<Participant> enu = rtpSession.partDb.getParticipants();
			while(enu.hasMoreElements()) {
				Participant part = enu.nextElement();
				if(part.lastRtpPkt > this.prevTime)
					senderCount++;
			}
			
			if(senderCount*2 > rtpSession.partDb.ssrcTable.size()) {
				if(rtpSession.lastTimestamp > this.prevTime) {
					//We're a sender
					double numerator = ((double) this.avgPktSize)*((double) senderCount);
					double denominator = 0.05*0.25* rtpSession.bandwidth;
					this.nextDelay = (int) Math.round((numerator/denominator)*randDouble);
				} else {
					//We're a receiver
					double numerator = ((double) this.avgPktSize)*((double) rtpSession.partDb.ssrcTable.size());
					double denominator = 0.05*0.75* rtpSession.bandwidth;
					this.nextDelay = (int) Math.round((numerator/denominator)*randDouble);
				}
			} else {
				double numerator = ((double) this.avgPktSize)*((double) rtpSession.partDb.ssrcTable.size());;
				double denominator = 0.05 * rtpSession.bandwidth;
				this.nextDelay = (int) Math.round(1000.0*(numerator/denominator)) + rand;
			}
		} else {
			// Not enough data to scale, use random values
			int rand = rtpSession.random.nextInt(1000) - 500; //between -500 and +500
			if(this.initial) {
				// 2.5 to 3.5 seconds, randomly
				this.nextDelay = 3000 + rand;
				this.initial = false;
			} else {
				// 4.5 to 5.5 seconds, randomly
				this.nextDelay = 5000 + rand;
			}

		}
		
		// preflight check
		if(this.nextDelay < 1000) {
			int rand = rtpSession.random.nextInt(1000) - 500; //between -500 and +500
			System.out.println("RTCPSession.calculateDelay() nextDelay was too short (" 
					+this.nextDelay+"ms), setting to "+(this.nextDelay = 2000 + rand));
		}
		this.prevTime = curTime;
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
