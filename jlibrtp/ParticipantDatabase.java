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
	Hashtable table = null; // Holds participants
	HashSet receivers = null; // InetSocketAddresses
	
	public ParticipantDatabase() {
		table = new Hashtable();
		receivers = new HashSet();
	}
	
	synchronized protected void addParticipant(Participant p) {
		table.put(p.ssrc, p);
		
		if(p.isReceiver && p.rtpAddress != null && !receivers.contains(p.rtpAddress)) {
			receivers.add(p.rtpAddress);
		}
	}
	
	synchronized protected void removeParticipant(Participant p) {
		table.remove(p.ssrc);
		
		if(p.isReceiver) {
			// Need to see whether more recipients use this address (multicast)
			boolean notused = true;
			Enumeration enu = table.elements();
			while(enu.hasMoreElements() && notused) {
				Participant ap = (Participant) enu.nextElement();
				if(ap.rtpAddress.equals(p.rtpAddress)) {
					notused = false;
				}
			}
			
			if(notused) {
				receivers.remove(p.rtpAddress);
			}
				
		}
	}
	
	synchronized protected void updateParticipant(Participant p) {
		this.removeParticipant(p);
		this.addParticipant(p);
	}
	
	protected Participant getParticipant(long ssrc) {
		Participant p = null;
		p = (Participant) table.get(ssrc);
		
		return p; 
	}
	
	protected Participant getParticipant(String cname) {
		Participant p;
		
		Enumeration enu = table.elements();
		while(enu.hasMoreElements()) {
			p = (Participant) enu.nextElement();
			
			if(p.cname.equalsIgnoreCase(cname)) {
				return p;
			}
			
		}
		return null;
	}
	
	protected Iterator getReceivers() {
		return receivers.iterator();
	}
	
	protected Enumeration getParticipants() {
		return table.elements();
	}
}
