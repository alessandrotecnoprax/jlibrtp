package jlibrtpDemos;

import jlibrtp.*;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.DatagramSocket;

//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.stream.StreamSource;

//import org.jdom.Attribute;
//import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
//import org.jdom.JDOMException;
//import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class XmlPacketRecorder implements RTPAppIntf, RTCPAppIntf {
		// For the session
		RTPSession rtpSession = null;
		// The number of packets we have received
		int packetCount = 0;
		final int maxPacketCount = 1000;
		
		// For the document
		Document sessionDocument = null;
		Element sessionElement = null;
		
		/**
		 * Constructor
		 */
		public XmlPacketRecorder() {
			DatagramSocket rtpSocket = null;
			DatagramSocket rtcpSocket = null;
			
			try {
				rtpSocket = new DatagramSocket(6002);
				rtcpSocket = new DatagramSocket(6003);
			} catch (Exception e) {
				System.out.println("RTPSession failed to obtain port");
			}
			
			
			this.rtpSession = new RTPSession(rtpSocket, rtcpSocket);
			this.rtpSession.RTPSessionRegister(this,this);
			
			Participant p = new Participant("127.0.0.1", 6004, 6005);
			this.rtpSession.addParticipant(p);
		}
		
		
		/**
		 * RTCP
		 */
		public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, 
				long rtpTimestamp, long packetCount, long octetCount ) {
				
			
			this.sessionElement.addContent(new Element("SR"));
			this.packetCount++;
		}
		
		public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, 
				int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, 
				long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
				
			this.sessionElement.addContent(new Element("RR"));			
			this.packetCount++;	
		}
		
		public void SDESPktReceived(Participant[] relevantParticipants) {
			//Do nothing
			this.sessionElement.addContent(new Element("SDES"));	
			this.packetCount++;	
		}
		
		public void BYEPktReceived(Participant[] relevantParticipants, String reason) {
			this.sessionElement.addContent(new Element("BYE"));	
			this.packetCount++;	
		}
		
		/**
		 * RTP
		 */
		public void receiveData(byte[] buff, Participant participant, long timeMs) {
			this.sessionElement.addContent(new Element("BYE"));	
			this.packetCount++;	
		}
		
		
		/**
		 * 
		 * @param args
		 */
	    public void createDocument() {
	        // Create the root element
	        this.sessionElement = new Element("RTPSession");
	        //create the document
	        this.sessionDocument = new Document(sessionElement);
	        //add an attribute to the root element
	        
	        Element sessionInformation = new Element("sessionInformation");
	        this.sessionElement.addContent(sessionInformation);
	        
	        Element ssrc = new Element("SSRC");
	        ssrc.addContent( Long.toString(this.rtpSession.getSsrc()));
	        sessionInformation.addContent(ssrc);
	        
	        Element cname = new Element("CNAME");
	        cname.addContent( rtpSession.getCNAME());
	        sessionInformation.addContent(cname);
	        
	        Element sessionStart = new Element("sessionStart");
	        sessionStart.addContent(Long.toString(System.currentTimeMillis()));
	        sessionStart.setAttribute("unit","ms");
	        sessionInformation.addContent(sessionStart);
	    }
	
		public static void main(String[] args) {
			XmlPacketRecorder recorder = new XmlPacketRecorder();
			recorder.createDocument();
			
			System.out.print("Waiting for packets");
			int prevCount = 0;
			while(recorder.packetCount < recorder.maxPacketCount) {	
				if(recorder.packetCount > prevCount)
					System.out.print(".");
				prevCount = recorder.packetCount;
				
				try { Thread.sleep(500); } catch (Exception e) { System.out.println("oops."); }
			}
			System.out.println();
			
			
			System.out.println("Writing XML");
			try {
				XMLOutputter outputter = new XMLOutputter();
				FileWriter writer = new FileWriter("/home/ak/XmlPacketRecorder.xml");
				outputter.output(recorder.sessionDocument, writer);
				writer.close();
	        } catch (java.io.IOException e) {
	            e.printStackTrace();
	        }
	        
	        recorder.rtpSession.endSession();
			System.out.println("All done.");
			try { Thread.sleep(250); } catch (Exception e) { System.out.println("oops."); }
			System.out.println(""+ Thread.activeCount());
		}
}
