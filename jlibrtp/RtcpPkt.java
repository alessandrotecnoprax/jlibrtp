package jlibrtp;

public class RtcpPkt {
	private boolean rawPktCurrent = false;
	private int version = 2; 		//2 bits
	private int padding; 			//1 bit
	private int itemCount;	 		//5 bits
	private int packetType;			//8 bits
	private long length;			//16 bits
	
	// Contains the actual data (eventually)
	private byte[] rawPkt = null;
	private byte[] information = null;
	
	
	
}
