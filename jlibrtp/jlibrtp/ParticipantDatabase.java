package jlibrtp;
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

import java.util.*;
	
public class ParticipantDatabase {
	Hashtable receivers = null; // Holds known receivers
	Hashtable senders = null; // Holds known senders
	Hashtable unknownSenders = null; // Holds unknown participants, ie unexpected packets.
	
	public ParticipantDatabase() {
		receivers = new Hashtable();
		senders = new Hashtable();
		unknownSenders = new Hashtable();
	}
	
	public void addParticipant(Participant p) {
		if(p.cname != null) {
			if(p.isReceiver) {
				receivers.put(p.simpleHash(), p);
			}
			if(p.isSender) {
				senders.put(p.simpleHash(), p);
			}
		} else {
			unknownSenders.put(p.simpleHash(), p);
		}
	}
	
	public void removeParticipant(Participant p) {
		// We adhere to a strict "no questions asked policy"
		receivers.remove(p.simpleHash());
		senders.remove(p.simpleHash());
		unknownSenders.remove(p.simpleHash());
	}
	
	public void updateParticipant(Participant p) {
		//This can be tricky, we'll do it the simple way:
		//Delete no matter what key, reinsert correctly.
		
		if(p.cname != null) {
			receivers.remove(p.cname.hashCode());
			senders.remove(p.cname.hashCode());
			unknownSenders.remove(p.cname.hashCode());
		}
		
		if(p.ssrc > 0) {
			receivers.remove(p.ssrc);
			senders.remove(p.ssrc);
			unknownSenders.remove(p.ssrc);
		}
		
		this.addParticipant(p);
	}
	
	public Participant getSender(long ssrc) {
		return (Participant) senders.get(ssrc);
	}
	
	public Participant getParticipant(long ssrc) {
		Participant p = null;
		p = (Participant) senders.get(ssrc);
		
		if(p == null) {
			p = (Participant) unknownSenders.get(ssrc);
		}
		if(p == null) {
			p = (Participant) receivers.get(ssrc);
		}
		
		return p;
	}
	
	public Participant getParticipant(String cname) {
		Participant p = null;
		
		p = (Participant) senders.get(cname.hashCode());
	
		if(p == null) {
			p = (Participant) receivers.get(cname.hashCode());
		}
		
		return p;
	}
	
	public Enumeration getReceivers() {
		return receivers.elements();
	}
	public Enumeration getSenders() {
		return senders.elements();
	}
	public Enumeration getUnknownSenders() {
		return unknownSenders.elements();
	}
}
