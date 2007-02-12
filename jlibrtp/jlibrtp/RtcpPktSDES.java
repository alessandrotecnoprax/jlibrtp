package jlibrtp;

import java.util.LinkedList;

public class RtcpPktSDES extends RtcpPkt {
	private long[] ssrcArray = null;//32xn bits, n<16
	protected LinkedList<byte[]> firstItemList = null;
	
	protected RtcpPktSDES() {
		// Fetch all the right stuff from the database
		
		
	}
	
	protected RtcpPktSDES(byte[] aRawPkt) {
		rawPkt = aRawPkt;

		//byte[] header = new byte[4];
		//System.arraycopy(aRawPkt, 0, header, 0, 4);

		if(super.parseHeaders() != 0 || packetType != 202) {
			//Error...
		} else {
			firstItemList = new LinkedList<byte[]>();
			int curPos = 4;
			int curLength;
			int curType;
			boolean endReached = false;
			// Loop over SSRC SDES chunks
			for(int i=0; i<super.itemCount; i++) {
				ssrcArray[i] = StaticProcs.combineBytes(aRawPkt[curPos],aRawPkt[curPos + 1],aRawPkt[curPos + 2],aRawPkt[curPos + 3]);
	
				curPos += 4;
				
				while(!endReached && (curPos/4) <= this.length) {
					curType = (int) aRawPkt[curPos];
					
					if(curType == 0) {	
						
						curPos += 4 - (curPos % 4);
						endReached = true;
						
					} else {
						curLength  = (int) aRawPkt[curPos + 1];
						
						byte[] item = new byte[curLength];
						System.arraycopy(aRawPkt, curPos + 1, item, 0, curLength);
						
						if(i==0)
							firstItemList.add(item);
						
						curPos = curPos + curLength + 2;
					}

				}
			}
		}
	}
	
	protected byte[] encode() {	
		return new byte[1];
	}
}
