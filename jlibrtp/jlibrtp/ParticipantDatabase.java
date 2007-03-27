/**
 * Java RTP Library
 * Copyright (C) 2006 Arne Kepp
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jlibrtp;

import java.util.*;
import java.net.*;

/**
 * The participant database maintains three hashtables with participants.
 * 
 * The key issue is to be fast for operations that happen every time an
 * RTP packet is sent or received. We allow linear searching in cases 
 * where we need to update participants with information.
 * 
 * The keying is therefore usually the SSRC. In cases where we have the
 * cname, but no SSRC is known (no SDES packet has been received), a
 * simple hash i calculated based on the CNAME. The RTCP code should,
 * when receiving SDES packets, check whether the participant is known
 * and update the copy in this database with SSRC if needed.
 * 
 * @author Arne Kepp
 */
public class ParticipantDatabase {
	RTPSession rtpSession = null;
	Hashtable ssrcTable = new Hashtable();
	Hashtable ipTable = new Hashtable();
	HashMap<InetSocketAddress, Integer> rtpReceivers = new HashMap<InetSocketAddress, Integer>();
	//HashMap<InetSocketAddress, Integer> rtcpReceivers = new HashMap<InetSocketAddress, Integer>();
	//HashSet<Participant> all = ;
	Set<Participant> all = Collections.synchronizedSet(new HashSet<Participant>());
	//Map m = Collections.synchronizedMap(new HashMap(...));
	int senderCount = 0;
	
	public ParticipantDatabase(RTPSession parent) {
		rtpSession = parent;
	}
	
	synchronized protected int addParticipant(Participant p) {
		if( p.rtpAddress != null && p.rtpAddress.getAddress().isMulticastAddress() != this.rtpSession.mcSession) {
			System.out.println("ParticipantDatabase.addParticipant() rejected (non)multicast participant.");
			return -1;
		}
		
		//All
		all.add(p);

		// Do we identify this one by socket address or SSRC? The latter is prefered.
		if(p.ssrc > 0) {
			ssrcTable.put(p.ssrc, p);
		} else if( ! rtpSession.mcSession) {
			ipTable.put(p.rtpAddress, p);
		}

		if(! rtpSession.mcSession) {
			// Add to RTP receivers?
			if(p.rtpAddress != null) {
				Integer anInt = rtpReceivers.get(p.rtpAddress);
				if(anInt != null) {
					anInt = new Integer(anInt.intValue() + 1) ;
					rtpReceivers.put(p.rtpAddress, anInt);
				} else {
					anInt = new Integer(1);
					rtpReceivers.put(p.rtpAddress, anInt);
				}
			}

			// Add to RTCP receivers?
			//if(p.rtcpAddress != null) {
			//	Integer anInt = rtcpReceivers.get(p.rtcpAddress);
			//	if(anInt != null) {
			//		anInt = new Integer(anInt.intValue() + 1);
			//		rtcpReceivers.put(p.rtcpAddress, anInt);
			//	} else {
			//		anInt = new Integer(1);
			//		rtcpReceivers.put(p.rtcpAddress, anInt);
			//	}
			//}
		}
		return 0;
	}
	
	synchronized protected void removeParticipant(Participant p) {
		//All
		all.remove(p);
		
		Participant tmp;
		if(p.ssrc > 0) {
			ssrcTable.remove(p.ssrc);
		}
		if(!rtpSession.mcSession && p.rtpAddress != null) {
			ipTable.remove(p.rtpAddress);
		}

		if(! rtpSession.mcSession ) {
			// Remove from RTP receivers?
			if(p.rtpAddress != null) {
				Integer anInt = rtpReceivers.get(p.rtpAddress);
				if(anInt != null) {
					if(anInt.intValue() > 1) {
						anInt = new Integer(anInt.intValue() - 1);
						rtpReceivers.put(p.rtpAddress, anInt);
					} else {
						rtpReceivers.remove(p.rtpAddress);
					}
				}
			}

			// Remove from RCTP receivers?
			//if(p.rtcpAddress != null) {
			//	Integer anInt = rtcpReceivers.get(p.rtcpAddress);
			//	if(anInt != null) {
			//		if(anInt.intValue() > 1) {
			//			anInt = new Integer(anInt.intValue() - 1);
			//			rtcpReceivers.put(p.rtcpAddress, anInt);
			//		} else {
			//			rtcpReceivers.remove(p.rtcpAddress);
			//		}
			//	}
			//}
		}

	}
	
	synchronized protected void updateParticipant(Participant p) {
		this.removeParticipant(p);
		this.addParticipant(p);
	}
	
	protected Participant getParticipant(long ssrc) {
		Participant p = null;
		p = (Participant) ssrcTable.get(ssrc);
		return p; 
	}
	
/**
 * This method looks for participants the application has added,
 * which we have yet to associate with an SSRC
 * 
 * @param anAddress
 * @return best guess for which Participant
 */
	protected Participant getParticipant(InetAddress anAddress) {
		Participant p = null;
		Enumeration enu = ipTable.elements();
		
		// We'll look for the first available match
		while(enu.hasMoreElements()) {
			Participant tmp = (Participant) enu.nextElement();
			if(tmp.rtpAddress.getAddress().equals(anAddress))
				p = tmp;
		}

		return p; 
	}
	
	protected Iterator getParticipants() {
		return all.iterator();
	}
	
	protected Iterator getRtpReceivers() {
		return rtpReceivers.keySet().iterator();
	}
	
	//protected Iterator getRtcpReceivers() {
	//	return rtpReceivers.keySet().iterator();
	//}
	
	protected int receiverCount() {
		return rtpReceivers.size();
	}
	
	protected int senderCount() {
		return senderCount;
	}
	
	protected void debugPrint() {
		System.out.println("   ParticipantDatabase.debugPrint()");
		Participant p;
		Enumeration enu = ssrcTable.elements();
		while(enu.hasMoreElements()) {
			p = (Participant) enu.nextElement();
			System.out.println("           ssrcTable ssrc:"+p.ssrc+" cname:"+p.cname
					+" loc:"+p.loc+" rtpAddress:"+p.rtpAddress+" rtcpAddress:"+p.rtcpAddress);
		}
		enu = ipTable.elements();
		while(enu.hasMoreElements()) {
			p = (Participant) enu.nextElement();
			System.out.println("           ipTable rtpAddress:"+p.rtpAddress+" rtcpAddress:"+p.rtcpAddress);
		}
		
		Set aSet = rtpReceivers.keySet();
		Iterator iter = aSet.iterator();
		while(iter.hasNext()) {
			InetSocketAddress inetAdr = (InetSocketAddress) iter.next();
			Integer anInt = rtpReceivers.get(inetAdr);
			System.out.println("           rtpReceivers: "+inetAdr.toString() + "  count:" + anInt.intValue());
		}
		//aSet = rtcpReceivers.keySet();
		//iter = aSet.iterator();
		//while(iter.hasNext()) {
		//	InetSocketAddress inetAdr = (InetSocketAddress) iter.next();
		//	Integer anInt = rtcpReceivers.get(inetAdr);
		//	System.out.println("           rtcpReceivers: "+inetAdr.toString() + "  count:" + anInt.intValue());
		//}
	}
}
