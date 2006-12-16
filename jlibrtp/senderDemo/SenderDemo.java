package senderDemo;
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


import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;
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

import java.lang.String;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jlibrtp.*;

public class SenderDemo implements RTPAppIntf  {
	public RTPSession rtpSession = null;
	static int pktCount = 0;
	private String filename;
	private final int EXTERNAL_BUFFER_SIZE = 320;
	SourceDataLine auline;
	private Position curPosition;
	boolean local;
	 enum Position {
		LEFT, RIGHT, NORMAL
	};
	
	public SenderDemo(String CNAME,int recvPort, boolean isLocal)  {
		try {
			rtpSession = new RTPSession(recvPort, recvPort +1, CNAME);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port: " + recvPort);
		}
		if(rtpSession != null) {
			rtpSession.RTPSessionRegister(this);
		} else {
			System.out.println("Couldn't register");
		}
		this.local = isLocal;
		//public Participant(String sendingHost,int port,String CNAME)
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for(int i=0;i<args.length;i++) {
			System.out.println("args["+i+"]" + args[i]);
		}
		if(args.length < 4) {
			System.out.println("Please specify filename, ip-address and ports.");
		} else {
		// TODO Auto-generated method stub
		System.out.println("Setup");
		boolean local = true;
		//if(0 != args[1].compareToIgnoreCase("127.0.0.1")) {
		//	local = false;
		//}
		SenderDemo aDemo = new SenderDemo("Sender",4547, local);
		Participant p = new Participant(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), "Receiver");
		aDemo.rtpSession.addParticipant(p);
		//aDemo.filename = "/usr/share/sounds/login.wav";
		aDemo.filename = args[0];
		aDemo.run();
		System.out.println("pktCount: " + pktCount);
		}
	}
	
	public void receiveData(byte[] dummy1, String dummy2, long dummy3) {
		// We don't expect any data.
	}
	
	public void run() {
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("-> Run()");
		} 
		File soundFile = new File(filename);
		if (!soundFile.exists()) {
			System.err.println("Wave file not found: " + filename);
			return;
		}

		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		//AudioFormat format = audioInputStream.getFormat();
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 8000.0), 16, 1, 2, ((float) 8000.0) ,false);
		System.out.println(format.toString());
		
		
		if(! this.local) {
			// To time the output correctly, we also play at the input:
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
		}
		
		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
		long start = System.currentTimeMillis();
		try {
			while (nBytesRead != -1) {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
				if (nBytesRead >= 0) {
					rtpSession.sendData(abData);
					if(!this.local) {	
						auline.write(abData, 0, abData.length);
					//System.out.println("pktCount:" + pktCount + " length:"  + abData.length + " hash:" + abData[0] + abData[2] + " nbytes: " + nBytesRead);
					} else {
						try { Thread.sleep(14);} catch(Exception e) {}
					}
					pktCount++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Time: " + (System.currentTimeMillis() - start)/1000 + " s");
		//rtpSession.endSession();
		if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("<- Run()");
		} 
	}

}
