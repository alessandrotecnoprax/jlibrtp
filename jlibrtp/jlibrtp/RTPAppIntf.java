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
 * This is the callback interface for RTP packets.
 * 
 * It is mandatory, but you can inore the data if you like.
 * 
 * @author Arne Kepp
 */
public interface RTPAppIntf {
	
	/**
	 * The callback method through which the application will receive
	 * data from jlibrtp. These calls are synchronous, so you will not
	 * receive any new packets until this call returns.
	 * 
	 * @param buff a byte-buffer containing the data received
	 * @param Participant participant
	 * @param timeMs the time when this packet was created, as set by the sender, converted to System.currentTimeMillis()
	 */
	public void receiveData(DataFrame frame, Participant participant);
	
	
	/**
	 * The callback method through which the application will receive
	 * notifications about user updates, additions and byes.
	 *  Types:
	 *  	1 - Bye
	 *  	2 - New through RTP, check .getRtpSendSock()
	 *  	3 - New through RTCP, check .getRtcpSendSock()
	 * 		4 - SDES packet received, check the getCname() etc methods
	 *      5 - Matched SSRC to ip-address provided by application
	 * 
	 * @param type the type of event
	 * @param participant(s) in question
	 */
	public void userEvent(int type, Participant[] participant);
}
