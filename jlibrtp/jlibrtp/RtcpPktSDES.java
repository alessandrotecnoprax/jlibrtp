package jlibrtp;

import java.util.LinkedList;

public class RtcpPktSDES extends RtcpPkt {
	boolean reportSelf = true;
	protected Participant[] participants = null;
	
	protected RtcpPktSDES(boolean reportThisSession, Participant[] additionalParticipants) {
		// Fetch all the right stuff from the database
		reportSelf = reportThisSession;
		participants = additionalParticipants;
	}
	
	protected RtcpPktSDES(byte[] aRawPkt, ParticipantDatabase partDb) {
		rawPkt = aRawPkt;

		if(super.parseHeaders() != 0 || packetType != 202) {
			//Error...
		} else {
			int curPos = 4;
			int curLength;
			int curType;
			long ssrc;
			boolean endReached = false;
			// Loop over SSRC SDES chunks
			for(int i=0; i< itemCount; i++) {
				ssrc = StaticProcs.combineBytes(aRawPkt[curPos],aRawPkt[curPos + 1],aRawPkt[curPos + 2],aRawPkt[curPos + 3]);
	
				Participant part = partDb.getParticipant(ssrc);
				if(part == null) {
					part = new Participant(ssrc);
					partDb.addParticipant(part);
				}
				curPos += 4;
				
				while(!endReached && (curPos/4) <= this.length) {
					curType = (int) aRawPkt[curPos];
					
					if(curType == 0) {	
						
						curPos += 4 - (curPos % 4);
						endReached = true;
						
					} else {
						curLength  = (int) aRawPkt[curPos + 1];

						if(curLength > 0) {
							byte[] item = new byte[curLength];
							System.arraycopy(aRawPkt, curPos + 1, item, 0, curLength);


							switch(curType) {
							case 1:  part.cname = new String(item); break;
							case 2:  part.name = new String(item); break;
							case 3:  part.email = new String(item); break;
							case 4:  part.phone = new String(item); break;
							case 5:  part.loc = new String(item); break;
							case 6:  part.tool = new String(item); break;
							case 7:  part.note = new String(item); break;
							case 8:  part.priv = new String(item); break;
							}
						} else {
							switch(curType) {
							case 1:  part.cname = null; break;
							case 2:  part.name = null; break;
							case 3:  part.email = null; break;
							case 4:  part.phone = null; break;
							case 5:  part.loc = null; break;
							case 6:  part.tool = null; break;
							case 7:  part.note = null; break;
							case 8:  part.priv = null; break;
							}

						}
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
