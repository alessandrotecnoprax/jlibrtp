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
import java.util.concurrent.*;

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

	LinkedList<Participant> receivers = new LinkedList<Participant>();	
	ConcurrentHashMap<Long,Participant> ssrcTable = new ConcurrentHashMap<Long,Participant>();
	
	public ParticipantDatabase(RTPSession parent) {
		rtpSession = parent;
	}
	
	/**
	 * 
	 * @param cameFrom 0: Application, 1: RTP packet, 2: RTCP
	 * @param p
	 * @return 0 if okay, -1 if 
	 */
	protected int addParticipant(int cameFrom, Participant p) {
		//Multicast or not?
		if(this.rtpSession.mcSession) {
			return this.addParticipantMulticast(cameFrom, p);
		} else {
			return this.addParticipantUnicast(cameFrom, p);
		}
		
	}
	
	private int addParticipantMulticast(int cameFrom, Participant p) {
		if( cameFrom == 0) {
			System.out.println("ParticipantDatabase.addParticipant() doesnt expect" 
					+ " application to add participants to multicast session.");
			return -1;
		} else {
			// Check this one is not redundant
			if(this.ssrcTable.contains(p.ssrc)) {
				System.out.println("ParticipantDatabase.addParticipant() SSRC "
						+"already known " + Long.toString(p.ssrc));
				return -2;
			} else {
				this.ssrcTable.put(p.ssrc, p);
				return 0;
			}
		}
	}
	
	private int addParticipantUnicast(int cameFrom, Participant p) {
		if(cameFrom == 0) {
			//Check whether there is a match in the ssrcTable
			boolean notDone = true;
			
			Enumeration<Participant> enu = this.ssrcTable.elements();
			while(notDone && enu.hasMoreElements()) {
				Participant part = enu.nextElement();
				if(part.unexpected && 
						(part.rtcpReceivedFromAddress.equals(part.rtcpAddress.getAddress()) 
						|| part.rtpReceivedFromAddress.equals(part.rtpAddress.getAddress()))) {
					
					part.rtpAddress = p.rtpAddress;
					part.rtcpAddress = p.rtcpAddress;
					part.unexpected = false;

					//Report the match back to the application
					Participant[] partArray = {part};
					this.rtpSession.appIntf.userEvent(5, partArray);
					
					notDone = false;
					p = part;
				}
			}

			//Add to the table of people that we send packets to
			this.receivers.add(p);
			return 0;
			
		} else {
			//Check whether there's a match in the receivers table
			boolean notDone = true;
			
			Iterator<Participant> iter = this.receivers.iterator();
			while(notDone && iter.hasNext()) {
				Participant part = iter.next();
				if((cameFrom == 1 && p.rtpReceivedFromAddress.equals(part.rtpAddress.getAddress()))
					|| (cameFrom == 2 && p.rtcpReceivedFromAddress.equals(part.rtcpAddress.getAddress()))) {
				
					p.rtpAddress = part.rtpAddress;
					p.rtcpAddress = part.rtcpAddress;
					p.unexpected = false;

					//Report the match back to the application
					Participant[] partArray = {part};
					this.rtpSession.appIntf.userEvent(5, partArray);
					
					//Remove the old one, add this one
					this.receivers.remove(part);
					this.receivers.add(p);
					notDone = false;
				}
			}
			
			this.ssrcTable.put(p.ssrc, p);				
			return 0;
		}
	}
	
	protected void removeParticipant(Participant p) {
		if(! this.rtpSession.mcSession)
			this.receivers.remove(p);
		
		this.ssrcTable.remove(p.ssrc, p);
	}
		
	
	protected Participant getParticipant(long ssrc) {
		Participant p = null;
		p = ssrcTable.get(ssrc);
		return p; 
	}
	
	protected Iterator<Participant> getUnicastReceivers() {
		if(this.rtpSession.mcSession) {
			return this.receivers.iterator();
		} else {
			System.out.println("Request for ParticipantDatabase.getUnicastReceivers in multicast session");
			return null;
		}
	}
	
	protected Enumeration<Participant> getParticipants() {
		return this.ssrcTable.elements();
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
		
		Iterator<Participant> iter = receivers.iterator();
		while(iter.hasNext()) {
			p = iter.next();
			System.out.println("           receivers: "+p.rtpAddress.toString());
		}
	}
}
