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
		final int maxPacketCount = 500;
		boolean noBye = true;
		
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
				rtpSocket = new DatagramSocket(16384);
				rtcpSocket = new DatagramSocket(16385);
			} catch (Exception e) {
				System.out.println("RTPSession failed to obtain port");
			}
			
			
			this.rtpSession = new RTPSession(rtpSocket, rtcpSocket);
			this.rtpSession.RTPSessionRegister(this,this);
			
			Participant p = new Participant("127.0.0.1", 16386, 16387);
			this.rtpSession.addParticipant(p);
			this.rtpSession.setNaivePktReception(true);
		}
		
		
		/**
		 * RTCP
		 */
		public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, 
				long rtpTimestamp, long packetCount, long octetCount ) {
			
			Element SRPkt = new Element("SRPkt");
			this.sessionElement.addContent(SRPkt);
			
			Element ArrivalTimestamp = new Element("ArrivalTimestamp");
			ArrivalTimestamp.addContent(Long.toString(System.currentTimeMillis()));
			SRPkt.addContent(ArrivalTimestamp);
			
			Element RTPTimestamp = new Element("RTPTimestamp");
			RTPTimestamp.addContent(Long.toString(rtpTimestamp));
			SRPkt.addContent(RTPTimestamp);
			
			Element NTPHigh = new Element("NTPHigh");
			NTPHigh.addContent(Long.toString(ntpHighOrder));
			SRPkt.addContent(NTPHigh);
			
			Element NTPLow = new Element("NTPLow");
			NTPLow.addContent(Long.toString(ntpLowOrder));
			SRPkt.addContent(NTPLow);
			
			Element SSRC = new Element("SSRC");
			SSRC.addContent(Long.toString(ssrc));
			SRPkt.addContent(SSRC);
			
			Element PacketCount = new Element("PacketCount");
			PacketCount.addContent(Long.toString(packetCount));
			SRPkt.addContent(PacketCount);
			
			Element OctetCount = new Element("OctetCount");
			OctetCount.addContent(Long.toString(octetCount));
			SRPkt.addContent(OctetCount);
			

			this.packetCount++;
		}
		
		public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, 
				int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, 
				long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
				
			this.sessionElement.addContent(new Element("RRPkt"));			
			this.packetCount++;	
		}
		
		public void SDESPktReceived(Participant[] relevantParticipants) {
			Element SDESPkt = new Element("SDESPkt");
			this.sessionElement.addContent(SDESPkt);
			
			if(relevantParticipants != null) {
				for(int i=0;i<relevantParticipants.length;i++) {
					Participant part = relevantParticipants[i];

					Element SDESBlock = new Element("SDESBlock");
					SDESPkt.addContent(SDESBlock);

					Element SSRC = new Element("SSRC");
					SSRC.addContent(Long.toString(part.getSSRC()));
					SDESBlock.addContent(SSRC);

					if(part.getCNAME() != null) {
						Element CNAME = new Element("CNAME");
						CNAME.addContent(part.getCNAME());
						SDESBlock.addContent(CNAME);
					}

					if(part.getNAME() != null) {
						Element NAME = new Element("NAME");
						NAME.addContent(part.getNAME());
						SDESBlock.addContent(NAME);
					}
					if(part.getEmail() != null) {
						Element EMAIL = new Element("EMAIL");
						EMAIL.addContent(part.getEmail());
						SDESBlock.addContent(EMAIL);
					}
					if(part.getPhone() != null) {
						Element PHONE = new Element("PHONE");
						PHONE.addContent(part.getPhone());
						SDESBlock.addContent(PHONE);
					}
					if(part.getLocation() != null) {
						Element LOC = new Element("LOC");
						LOC.addContent(part.getLocation());
						SDESBlock.addContent(LOC);
					}
					if(part.getNote() != null) {
						Element NOTE = new Element("NOTE");
						NOTE.addContent(part.getNote());
						SDESBlock.addContent(NOTE);
					}
					if(part.getPriv() != null) {
						Element PRIV = new Element("PRIV");
						PRIV.addContent(part.getPriv());
						SDESBlock.addContent(PRIV);
					}
					if(part.getTool() != null) {
						Element TOOL = new Element("TOOL");
						TOOL.addContent(part.getTool());
						SDESBlock.addContent(TOOL);
					}
				}
			} else {
				System.out.println("SDES with no participants?");
			}

			this.packetCount++;	
		}
		
		public void BYEPktReceived(Participant[] relevantParticipants, String reason) {
			//System.out.println("BYE!");
			Element BYEPkt = new Element("BYEPkt");
			this.sessionElement.addContent(BYEPkt);
			
			if(relevantParticipants != null) {
				for(int i=0;i<relevantParticipants.length;i++) {
					Element Participant = new Element("Participant");
					BYEPkt.addContent(Participant);
					
					Element SSRC = new Element("SSRC");
					SSRC.addContent(Long.toString(relevantParticipants[i].getSSRC()));
					Participant.addContent(SSRC);
					
					if(relevantParticipants[i].getCNAME() != null) {
						Element CNAME = new Element("CNAME");
						CNAME.addContent(relevantParticipants[i].getCNAME());
						Participant.addContent(CNAME);
					}					
				}
			}
			if(reason != null) {
				Element Reason = new Element("Reason");
				Reason.addContent(reason);
				BYEPkt.addContent(Reason);
			}
			
			this.packetCount++;	
			this.noBye = false;
		}
		
		/**
		 * RTP
		 */
		public void receiveData(DataFrame frame, Participant part) {
			//System.out.println(" RECEIVING RECEIVING ");
			Element RTPPkt = new Element("RTPpacket");
			this.sessionElement.addContent(RTPPkt);
			
			Element ArrivalTimestamp = new Element("ArrivalTimestamp");
			ArrivalTimestamp.addContent(Long.toString(System.currentTimeMillis()));
			RTPPkt.addContent(ArrivalTimestamp);
			
			Element RTPTimestamp = new Element("RTPTimestamp");
			RTPTimestamp.addContent(Long.toString(frame.getRTPTimestamp()));
			RTPPkt.addContent(RTPTimestamp);
			
			if(frame.getTimestamp() > 0) {
				Element Timestamp = new Element("Timestamp");
				Timestamp.addContent(Long.toString(frame.getTimestamp()));
				RTPPkt.addContent(Timestamp);
			}
			
			Element PayloadType = new Element("PayloadType");
			PayloadType.addContent(Integer.toString(frame.getPayloadType()));
			RTPPkt.addContent(PayloadType);
			
			Element Marked = new Element("Marked");
			Marked.addContent(Boolean.toString(frame.firstPacketMarked()));
			RTPPkt.addContent(Marked);
			
			Element SSRC = new Element("SSRC");
			SSRC.addContent(Long.toString(frame.getSSRC()));
			RTPPkt.addContent(SSRC);
			
			long[] csrcArray = frame.getCSRCs();
			for(int i=0; i< csrcArray.length; i++) {
				Element CSRC = new Element("CSRC");
				CSRC.addContent(Long.toString(csrcArray[i]));
				RTPPkt.addContent(CSRC);
			}
			
			Element Payload = new Element("Payload");
			byte[] payload = frame.getData();
			StringBuffer buf = new StringBuffer();
			for(int i=0; i<payload.length && i<64; i++ ) {
				buf.append(StaticProcs.hexOfByte(payload[i]));
			}
			Payload.addContent(buf.toString());
			RTPPkt.addContent(Payload);
			
			this.packetCount++;
			//if(packetCount == 100) {
			//	System.out.println("Time!!!!!!!!! " + Long.toString(System.currentTimeMillis()));
			//}
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
			while(recorder.packetCount < recorder.maxPacketCount && recorder.noBye) {	
				if(recorder.packetCount > prevCount)
					System.out.print(".");
				prevCount = recorder.packetCount;
				
				try { Thread.sleep(500); } catch (Exception e) { System.out.println("oops."); }
			}
			System.out.println();
			
			try { Thread.sleep(200); } catch (Exception e) { System.out.println("oops."); }
			
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
