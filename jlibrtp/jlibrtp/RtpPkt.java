package jlibrtp;

public class RtpPkt {
	private boolean rawPktCurrent = false;
	private int version = 2; 		//2 bits
	private int padding; 			//1 bit
	private int extension = 0; 		//1 bit
	private int marker = 0;			//1 bit
	private int payloadType;		//7 bits
	private int seqNumber;			//16 bits
	private long timeStamp;			//32 bits
	private long ssrc;				//32 bits
	private long[] csrcArray = null;//32xn bits, n<16
	
	// Contains the actual data (eventually)
	private byte[] rawPkt = null;
	private byte[] payload = null;
	/// BIG QUESTIONS
	// -2 How to deal when buffer has not been parsed
	// force it under instanciation? YES
	// How do we deal with padding internally in Java.
	// Are all codecs using complete bytes? Fixed length?
	// 1) Should padding be calculated dynamically
	// 2) Payload types, should it be more than a integer here?
	// 3) Which ones of these should be smaller?
	// bool is actually a 32 bit integer, using only one bit
	// byte is 8 bit, 2s complement, short is 16 bit, int is 32 bit
	// Could use char for 16 bit unsigned, lots of casting though
	// 4) Offer to do sequence number and time internally?
	//
	/**
	 * Construct an empty packet.
	 *  @return An empty packet.
	 */
	public RtpPkt(){
		// Nothing
	}
	/**
	 * Construct a packet-instance. The ByteBuffer required for UDP transmission can afterwards be obtained from getRawPkt(). If you need to set additional parameters, such as the marker bit or contributing sources, you should do so before calling getRawPkt;
	 *
	 * @param aTimeStamp RTP timestamp for data
	 * @param anSsrc Synchronization source
	 * @param seqNum Sequency number
	 * @param plt Type of payload
	 * @param pl Payload, the actual data
	 * @return A packet-instance.
	 */
	public RtpPkt(long aTimeStamp, long syncSource, int seqNum, int plt, byte[] pl){
		int test = 0;
		test += setTimeStamp(aTimeStamp);
		test += setSsrc(syncSource);
		test += setSeqNumber(seqNum);
		test += setPayloadType(plt);
		test += setPayload(pl);
		if(test != 0) {
			System.out.println("RtpPkt() failed, check with checkPkt()");
		}
		rawPktCurrent = true;
		if( RTPSession.rtpDebugLevel > 5) {
			System.out.println("<--> RtpPkt(aTimeStamp, syncSource, seqNum, plt, pl)"); 
		}
	}
	/**
	 * Construct a packet-instance from an raw packet (believed to be RTP). The UDP-headers must be removed before invoking this method. Call checkPkt on the instance to verify that it was successfully parsed.
	 *
	 * @param aRawPkt The data-part of a UDP-packet believed to be RTP 
	 * @return A packet-instance.
	 */
	public RtpPkt(byte[] aRawPkt){
		if( RTPSession.rtpDebugLevel > 5) {
			System.out.println("-> RtpPkt(aRawPkt)"); 
		}
		//Check size, need to have at least a complete header
		if(aRawPkt == null) {
			System.out.println("RtpPkt(byte[]) Packet null");
		}
		
		int remOct = aRawPkt.length - 12;
		if(remOct >= 0) {
			rawPkt = aRawPkt;	//Store it
			//Interrogate the packet
			sliceFirstLine();
			if(version == 2) {
				sliceTimeStamp();
				sliceSSRC();
				if(remOct > 4 && getCsrcCount() > 0) {
					sliceCSRCs();
					remOct -= csrcArray.length * 4; //4 octets per CSRC
				}
				// TODO Extension
				if(remOct > 0) {
					slicePayload(remOct);
				}
			
				//Sanity checks
				checkPkt();
		
				//Mark the buffer as current
				rawPktCurrent = true;
			} else {
				System.out.println("RtpPkt(byte[]) Packet is not version 2, giving up.");
			}
		} else {
			System.out.println("RtpPkt(byte[]) Packet too small to be sliced");
		}
		rawPktCurrent = true;
		if( RTPSession.rtpDebugLevel > 5) {
			System.out.println("<- RtpPkt(aRawPkt)");
		}
	}
	
	/*********************************************************************************************************
	 *                                                Reading stuff 
	 *********************************************************************************************************/
	public int checkPkt() {
		//TODO, check for version 2 etc
		return 0;
	}
	public int getHeaderLength() {
		//TODO include extension
		return 12 + 4*getCsrcCount();
	}
	public int getPayloadLength() {
		return payload.length;
	}
	//public int getPaddingLength() {
	//	return lenPadding;
	//}
	public int getVersion() {
		return version;
	}
	//public boolean isPadded() {
	//	if(lenPadding > 0) {
	//		return true;
	//	}else {
	//		return false;
	//	}
	//}
	//public int getHeaderExtension() {
	//TODO
	//}
	public boolean isMarked() {
		return (marker != 0);
	}
	public int getPayloadType() {
		return payloadType;
	}
	
	public int getSeqNumber() {
		return seqNumber;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public long getSsrc() {
		return ssrc;
	}
	
	public int getCsrcCount() {
		if(csrcArray != null) {
			return csrcArray.length;
		}else{
			return 0;
		}
	}
	public long[] getCsrcArray() {
		return csrcArray;
	}

	public byte[] encode() {
		if(! rawPktCurrent || rawPkt == null) {
			writePkt();
		} 
		return rawPkt;
	}
	
	public void printPkt() {
		System.out.print("V:" + version + " P:" + padding + " EXT:" + extension);
		System.out.println(" CC:" + getCsrcCount() + " M:"+ marker +" PT:" + payloadType + " SN: "+ seqNumber);
		System.out.println("Timestamp:" + timeStamp + "(long output as int, may be 2s complement)");
		System.out.println("SSRC:" + ssrc + "(long output as int, may be 2s complement)");
		for(int i=0;i<getCsrcCount();i++) {
			System.out.println("CSRC:" + csrcArray[i] + "(long output as int, may be 2s complement)");
		}
		//TODO Extension
		System.out.println("Payload, first four bytes: " + payload[0] + " " + payload[1] + " " + payload[2] + " " + payload[3]);
	}
	/*********************************************************************************************************
	 *                                                Setting stuff 
	 *********************************************************************************************************/
	
	//public int setVersion(int aVersion) {
	//	if(aVersion < 4 && aVersion > -1) {
	//		rawPktCurrent = false;
	//		version = aVersion;
	//		return 0;
	//	} else {
	//		System.out.println("RtpPkt.setVersion must be 0 <= x <= 3");
	//		return -1;
	//	}
	//}
	//public boolean isPadded(boolean ) {
	// Dynamically set	
	//}
	public void setMarked(boolean mark) {
		rawPktCurrent = false;
		if(mark) {
			marker = 1;
		} else {
			marker = 0;
		}
	}
	
	//public int setHeaderExtension() {
	//TODO
	//}
	
	//public int settCsrcCount() {
	// Dynamically set
	//}
	
	public int setPayloadType(int plType) {
		int temp = (plType & 0x0000007F); // 7 bits
		//System.out.println("PayloadType: " + plType + " temp:" + temp);
		if(temp == plType) {
			rawPktCurrent = false;
			payloadType = temp;
			return 0;
		} else {
			return -1;
		}
	}
	
	public int setSeqNumber(int number) {
		if(number <= 65536 && number >= 0) {
			rawPktCurrent = false;
			seqNumber = number;
			return 0;
		} else {
			System.out.println("RtpPkt.setSeqNumber: invalid number");
			return -1;
		}
	}
	
	public int setTimeStamp(long time) {
		rawPktCurrent = false;
		timeStamp = time;
		return 0;	//Naive for now
	}
	
	public int setSsrc(long source) {
		rawPktCurrent = false;
		ssrc = source;
		return 0;	//Naive for now
	}
	
	public int setCsrcs(long[] contributors) {
		if(contributors.length <= 16) {
			csrcArray = contributors;
			return 0;
		} else {
			System.out.println("RtpPkt.setCsrcs: Cannot have more than 16 CSRCs");
			return -1;
		}
	}
	
	public int setPayload(byte[] data) {
		// TODO Padding
		if(data.length < (1500 - 12)) {
			rawPktCurrent = false;
			payload = data;
			return 0;
		} else {
			System.out.println("RtpPkt.setPayload: Cannot carry more than 1480 bytes for now.");
			return -1;
		}
	}
	public byte[] getPayload() {
		return payload;
		}

	/*********************************************************************************************************
	 *                                           Private functions 
	 *********************************************************************************************************/
	private void writePkt() {
		int bytes = getPayloadLength();
		int headerLen = getHeaderLength();
		int csrcLen = getCsrcCount();
		rawPkt = new byte[headerLen + bytes];
		
		// The first line contains, version and various bits
		writeFirstLine();
		byte[] someBytes = StaticProcs.longToByteWord(timeStamp);
		for(int i=0;i<4;i++) {
			rawPkt[i + 4] = someBytes[i];
		}
		//System.out.println("writePkt timeStamp:" + rawPkt[7]);
		
		someBytes = StaticProcs.longToByteWord(ssrc);
		for(int i=0;i<4;i++) {
			rawPkt[i + 8] = someBytes[i];
		}
		//System.out.println("writePkt ssrc:" + rawPkt[11]);
		
		for(int j=0; j<csrcLen ; j++) {
			someBytes = StaticProcs.longToByteWord(csrcArray[j]);
			for(int i=0;i<4;i++) {
				rawPkt[i + 12 + 4*j] = someBytes[i];
			}
		}
		// TODO Extension

		//Payload
		System.arraycopy(payload, 0, rawPkt, headerLen, bytes);
		rawPktCurrent = true;
	}
	
	private void writeFirstLine() {
		byte aByte = 0;
		aByte |=(version << 6);
		aByte |=(padding << 5);
		aByte |=(extension << 4);
		aByte |=(getCsrcCount());
		rawPkt[0] = aByte;
		aByte = 0;
		aByte |=(marker << 7);
		aByte |= payloadType;
		rawPkt[1] = aByte;
		byte[] someBytes = StaticProcs.intToByteWord(seqNumber);
		rawPkt[2] = someBytes[2];
		rawPkt[3] = someBytes[3];
	}
	
	private void sliceFirstLine() {
		version = ((rawPkt[0] & 0xC0) >>> 6);
		padding = ((rawPkt[0] & 0x20) >>> 5);
		extension = ((rawPkt[0] & 0x10) >>> 4);
		csrcArray = new long[(rawPkt[0] & 0x0F)];
		marker = ((rawPkt[1] & 0x80) >> 7);
		payloadType = (rawPkt[1] & 0x7F);
		seqNumber = (int) rawPkt[2];
		seqNumber = seqNumber*256;
		seqNumber += (int) rawPkt[3];
	}
	private void sliceTimeStamp() {
		//System.out.println("sliceTimeStamp:" + rawPkt[7]);
		timeStamp = StaticProcs.combineBytes(rawPkt[4],rawPkt[5],rawPkt[6],rawPkt[7]);
	}
	private void sliceSSRC() {
		//System.out.println("sliceSSRC:" + rawPkt[11]);
		ssrc = StaticProcs.combineBytes(rawPkt[8],rawPkt[9],rawPkt[10],rawPkt[11]);

	}
	private void  sliceCSRCs() {
		// TODO add checks that the buffer is big enough
		for(int i=0; i< csrcArray.length; i++) {
			ssrc = StaticProcs.combineBytes(rawPkt[i*4 + 12],rawPkt[i*4 + 13],rawPkt[i*4 + 14],rawPkt[i*4 + 15]);
		}
	}
	//Extensions //TODO
	private void slicePayload(int bytes) {
		payload = new byte[bytes];
		int headerLen = getHeaderLength();
		
		System.arraycopy(rawPkt, headerLen, payload, 0, bytes);
	}
}	