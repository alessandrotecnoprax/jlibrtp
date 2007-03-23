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


/**
 * This is the callback interface for RTCP packets.
 * 
 * It is optional, you do not have to register it.
 * 
 * If there are specific events you wish to ignore,
 * you can simply implement empty functions.
 * 
 * These are all syncrhonous, make sure to return quickly
 * or do the handling in a new thread.
 * 
 * @author Arne Kepp
 */
public interface RTCPAppIntf {
	
	public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, 
			long rtpTimestamp, long packetCount, long octetCount );
	
	public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, 
			int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, 
			long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR);
	
	public void SDESPktReceived(Participant[] relevantParticipants);
	
	public void BYEPktReceived(Participant[] relevantParticipants, String reason);
}