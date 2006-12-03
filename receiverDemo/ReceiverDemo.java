package receiverDemo;

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
import jlibrtp.*;


public class ReceiverDemo implements RTPAppIntf {
	//test
	RTPSessionIntf rtpSession = null;
	private Position curPosition;
	private final int EXTERNAL_BUFFER_SIZE = 1024; // 1 Kbyte
	byte[] abData = null;
	int nBytesRead = 0;
	static int pktCount = 0;
	SourceDataLine auline;
	
	enum Position {
		LEFT, RIGHT, NORMAL
	};

	public void receiveData(byte[] data) {
		System.out.println("pktCount : " + pktCount);
		//int test = (int) data[0] + data[5] + data[6] + data[102];
		if(data != null && data.length > 0 && (data[0] + data[5] + data[6] + data[102] ) >0) {
			abData = data;
			nBytesRead = data.length;
			pktCount++;	
		}
	}
	
	public ReceiverDemo(String CNAME,int recvPort,String recvAddr)  {
		rtpSession = new RTPSession();
		rtpSession.RTPSessionRegister(CNAME,recvPort,this);	
		//public Participant(String sendingHost,int port,String CNAME)
		//Participant p = new Participant(recvAddr,recvPort, CNAME);
		//p.setIsSender();
		//rtpSession.addParticipant(p);
		//this.start();
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 44100.0), 16, 2, 4, ((float) 44100.0) ,false);
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
				//System.out.println("n");
				if(nBytesRead > 0) {
					auline.write(abData, 0, nBytesRead);
					nBytesRead = 0;
				}
			}
		} finally {
			auline.drain();
			auline.close();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Setup");
		ReceiverDemo aDemo = new ReceiverDemo("Test",4545,"127.0.0.1");
	}
	
	public void run() {
		System.out.println("-> Run()");
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 44100.0), 16, 2, 4, ((float) 44100.0) ,false);
		System.out.println(format.toString());
		SourceDataLine auline = null;
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
			if (curPosition == Position.RIGHT)
				pan.setValue(1.0f);
			else if (curPosition == Position.LEFT)
				pan.setValue(-1.0f);
		} 


		System.out.println("<- Run()");
	}

}
