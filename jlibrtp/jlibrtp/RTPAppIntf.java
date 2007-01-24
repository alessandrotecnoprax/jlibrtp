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
 * 
 * 
 * The instance holds a participant database, as well as other information about the session. 
 * When the application registers with the session, the necessary threads for receiving and 
 * processing RTP packets are spawned.
 * 
 * RTP Packets are sent synchronously, all other operations are asynchronous.
 * 
 * @author Arne Kepp
 */
public interface RTPAppIntf {
	
	/**
	 * The callback method through which the application will receive
	 * data from jlibrtp.
	 * 
	 * @param buff a byte-buffer containing the data received
	 * @param cName the cName (as determined through SSRC lookup) who sent it
	 * @param time the time when this packet was created, as set by the sender 
	 */
	public void receiveData(byte[] buff, String cName, long time);
	
}
