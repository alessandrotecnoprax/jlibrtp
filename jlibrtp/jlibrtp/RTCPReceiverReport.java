package jlibrtp;

public class RTCPReceiverReport
{
	long ssrc; 
	int fractionlost;
	int[] packetslost = new int[3];
	long exthighseqnr;
	long jitter;
	long lsr;
	long dlsr;
}
