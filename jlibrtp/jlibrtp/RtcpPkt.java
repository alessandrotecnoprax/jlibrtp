package jlibrtp;

public class RtcpPkt {
	protected boolean rawPktCurrent = false;
	protected int version = 2; 		//2 bits
	protected int padding; 			//1 bit
	protected int itemCount;	 		//5 bits
	protected int packetType;			//8 bits
	protected int length;				//16 bits
	
	// Contains the actual data (eventually)
	protected byte[] rawPkt = null;
	
	protected int getLength() {
		return length;
	}
	
	protected int parseHeaders() {
		version = ((rawPkt[0] & 0xC0) >>> 6);
		padding = ((rawPkt[0] & 0x20) >>> 5);
		itemCount = (rawPkt[0] & 0x1F);
		packetType = (int) rawPkt[1];
		length = StaticProcs.combineBytes(rawPkt[2], rawPkt[3]);
		
		if(version == 2 && packetType < 205 && packetType > 199 && length < 65536) {
			return 0;
		} else {
			return -1;
		}
	}
	
}
