package jlibrtp;

import java.net.InetSocketAddress;

public class RtcpPktSDES extends RtcpPkt {
	boolean reportSelf = true;
	RTPSession rtpSession = null;
	protected Participant[] participants = null;
	
	protected RtcpPktSDES(boolean reportThisSession, RTPSession rtpSession,Participant[] additionalParticipants) {
		super.packetType = 202;
		// Fetch all the right stuff from the database
		reportSelf = reportThisSession;
		participants = additionalParticipants;
		this.rtpSession = rtpSession; 
	}
	
	protected RtcpPktSDES(byte[] aRawPkt,int start, InetSocketAddress socket, ParticipantDatabase partDb) {
		if(RTPSession.rtpDebugLevel > 8) {
			System.out.println("  -> RtcpPktSDES(byte[], ParticipantDabase)");
		}
		rawPkt = aRawPkt;

		if(! super.parseHeaders(start) || packetType != 202) {
			if(RTPSession.rtpDebugLevel > 2) {
				System.out.println(" <-> RtcpPktSDES.parseHeaders() etc. problem");
			}
			super.problem = -202;
		} else {
			int curPos = 4 + start;
			int curLength;
			int curType;
			long ssrc;
			boolean endReached = false;
			this.participants = new Participant[itemCount];
			
			// Loop over SSRC SDES chunks
			for(int i=0; i< itemCount; i++) {
				ssrc = StaticProcs.bytesToUIntLong(aRawPkt, curPos);
				Participant part = partDb.getParticipant(ssrc);
				if(part == null) {
					if(RTPSession.rtpDebugLevel > 1) {
						System.out.println("RtcpPktSDES(byte[], ParticipantDabase) adding new participant, ssrc:"+ssrc);
					}
					
					//InetSocketAddress nullSocket = null;
					//This needs to be sorted out anyway
					part = new Participant(socket, socket , ssrc);
					partDb.addParticipant(2,part);
				}
				curPos += 4;
				
				this.participants[i] = part;
				
				while(!endReached && (curPos/4) < this.length) {
					//System.out.println("endReached " + endReached + " curPos: " + curPos + " length:" + this.length);
					curType = (int) aRawPkt[curPos];
					
					if(curType == 0) {	
						curPos += 4 - (curPos % 4);
						endReached = true;
					} else {
						curLength  = (int) aRawPkt[curPos + 1];
						//System.out.println("curPos:"+curPos+" curType:"+curType+" curLength:"+curLength+" read from:"+(curPos + 1));

						if(curLength > 0) {
							byte[] item = new byte[curLength];
							//System.out.println("curPos:"+curPos+" arawPkt.length:"+aRawPkt.length+" curLength:"+curLength);
							System.arraycopy(aRawPkt, curPos + 2, item, 0, curLength);

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
		if(RTPSession.rtpDebugLevel > 8) {
			System.out.println("  <- RtcpPktSDES()");
		}
	}
	
	// For the time being we'll only advertise ourselves.
	protected void encode() {	
		byte[] temp = new byte[1450];
		byte[] someBytes = StaticProcs.uIntLongToByteWord(this.rtpSession.ssrc);
		System.arraycopy(someBytes, 0, temp, 4, 4);
		int pos = 8;
	
		String tmpString = null;
		for(int i=1; i<9;i++) {
			switch(i) {
				case 1:  tmpString = this.rtpSession.cname; break;
				case 2:  tmpString = this.rtpSession.name; break;
				case 3:  tmpString = this.rtpSession.email; break;
				case 4:  tmpString = this.rtpSession.phone; break;
				case 5:  tmpString = this.rtpSession.loc; break;
				case 6:  tmpString = this.rtpSession.tool; break;
				case 7:  tmpString = this.rtpSession.note; break;
				case 8:  tmpString = this.rtpSession.priv; break;
			}
			
			if(tmpString != null) {
				someBytes = tmpString.getBytes();
				temp[pos] = (byte) i;
				temp[pos+1] = (byte) someBytes.length;
				System.arraycopy(someBytes, 0, temp, pos + 2, someBytes.length);
				//System.out.println("i: "+i+" pos:"+pos+" someBytes.length:"+someBytes.length);
				pos = pos + someBytes.length + 2;
			}
		}
		int leftover = pos % 4;
		if(leftover == 1) {
			temp[pos] = (byte) 0; 
			temp[pos + 1] = (byte) 1; 
			pos += 3;
		} else if(leftover == 2) {
			temp[pos] = (byte) 0; 
			temp[pos + 1] = (byte) 0; 
			pos += 2;
		} else if(leftover == 3) {
			temp[pos] = (byte) 0; 
			temp[pos + 1] = (byte) 3; 
			pos += 5;
		}
		
		
		// Here we ought to loop over participants, if we're doing SDES for other participants.
		
		super.rawPkt = new byte[pos];
		itemCount = 1;
		System.arraycopy(temp, 0, super.rawPkt, 0, pos);
		writeHeaders();
	}
	
	public void debugPrint() {
		System.out.println("RtcpPktSDES.debugPrint() ");
		if(participants != null) {
			for(int i= 0; i<participants.length; i++) {
				Participant part = participants[i];
				System.out.println("     part.ssrc: " + part.ssrc + "  part.cname: " + part.cname);
			}
		} else {
			System.out.println("     nothing to report (only valid for received packets)");
		}
	}
}
