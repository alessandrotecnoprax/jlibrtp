package receiverDemo;
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


import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.concurrent.locks.*;

import jlibrtp.*;


public class ReceiverDemo implements RTPAppIntf {
	//test
	RTPSession rtpSession = null;
	private Position curPosition;
	private final int EXTERNAL_BUFFER_SIZE = 320; // 1 Kbyte
	byte[] abData = null;
	int nBytesRead = 0;
	int pktCount = 0;
	int offsetCount = 0;
	SourceDataLine auline;
	
	final public Lock dataLock = new ReentrantLock();
	final public Condition dataReady = dataLock.newCondition();
	
	 enum Position {
		LEFT, RIGHT, NORMAL
	};

	public void receiveData(byte[] data, String cname, long time) {
		//System.out.println("receiveData, time:" + time );
		auline.write(data, 0, data.length);
		pktCount++;
		//System.out.println("pktcount:" + pktCount + "  " + auline.getBufferSize() + " " + auline.available() );
		//if(pktCount == 1562) {
		//	System.out.println("Received 1562 packets");
		//}
		//dataLock.lock();
	    //try { dataReady.signalAll(); } finally {
	    //	dataLock.unlock();
	    //}
	}
	
	public ReceiverDemo(String CNAME,int recvPort)  {
		try {
			rtpSession = new RTPSession(recvPort, recvPort + 1, CNAME);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port: " + recvPort);
		}
		if(rtpSession != null) {
			rtpSession.RTPSessionRegister(this);
		} else {
			System.out.println("Couldn't register");
		}
		//public Participant(String sendingHost,int port,String CNAME)
		//Participant p = new Participant(recvAddr,recvPort, CNAME);
		//p.setIsSender();
		//rtpSession.addParticipant(p);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Setup");
		ReceiverDemo aDemo = new ReceiverDemo("Test",4545);
		aDemo.doStuff();
		System.out.println("Done");
	}
	
	public void doStuff() {
		System.out.println("-> ReceiverDemo.doStuff()");
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 8000.0), 16, 1, 2, ((float) 8000.0) ,false);
		System.out.println(format.toString());
		auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		
		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if (auline.isControlSupported(FloatControl.Type.PAN)) {
			FloatControl pan = (FloatControl) auline
					.getControl(FloatControl.Type.PAN);
			if (this.curPosition == Position.RIGHT)
				pan.setValue(1.0f);
			else if (this.curPosition == Position.LEFT)
				pan.setValue(-1.0f);
		}
		
		auline.start();
		try {
			while (nBytesRead != -1) {
				if(abData != null) {
					auline.write(abData, 0, abData.length);
					abData = null;
				}
				try {
					dataLock.lock();
					try { dataReady.await(); } catch (Exception e) {};
				} finally {
					dataLock.unlock();
				}
			}
		} finally {
			auline.drain();
			auline.close();
		}
	}
}
