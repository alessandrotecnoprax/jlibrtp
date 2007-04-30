package jlibrtpDemos;

import jlibrtp.*;
import java.io.FileWriter;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class XmlPacketRecorder implements RTPAppIntf, RTCPAppIntf, DebugAppIntf {
		// For the session
		RTPSession rtpSession = null;
		// The number of packets we have received
		int packetCount = 0;
		int maxPacketCount = -1;
		boolean noBye = true;
		
		// For the document
		Document sessionDocument = null;
		Element sessionElement = null;
		
		/**
		 * Constructor
		 */
		public XmlPacketRecorder(int rtpPortNum, int rtcpPortNum, int maxPacketCount) {
			DatagramSocket rtpSocket = null;
			DatagramSocket rtcpSocket = null;
			this.maxPacketCount = maxPacketCount;
			
			try {
				rtpSocket = new DatagramSocket(rtpPortNum);
				rtcpSocket = new DatagramSocket(rtcpPortNum);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("RTPSession failed to obtain port");
			}
			
			
			this.rtpSession = new RTPSession(rtpSocket, rtcpSocket);
			this.rtpSession.RTPSessionRegister(this,this, this);
			
			//Participant p = new Participant("127.0.0.1", 16386, 16387);
			//this.rtpSession.addParticipant(p);
			this.rtpSession.naivePktReception(true);
		}
		
		public void debugPacketReceived(int type, InetSocketAddress socket, String description) {
			System.out.println("***** " + description);
		}
		
		public void debugPacketSent(int type, InetSocketAddress socket, String description) {
			System.out.println("***** " + description);
		}
		
		/**
		 * RTCP
		 */			
		public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, 
				long rtpTimestamp, long packetCount, long octetCount,
				// Get the receiver reports, if any
				long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, 
				long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
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
			RTPTimestamp.addContent(Long.toString(frame.rtpTimestamp()));
			RTPPkt.addContent(RTPTimestamp);
			
			Element SequenceNumber = new Element("SequenceNumber");
			int[] seqNums = frame.sequenceNumbers();
			SequenceNumber.addContent(Long.toString(seqNums[0]));
			RTPPkt.addContent(SequenceNumber);
			
			if(frame.timestamp() > 0) {
				Element Timestamp = new Element("Timestamp");
				Timestamp.addContent(Long.toString(frame.timestamp()));
				RTPPkt.addContent(Timestamp);
			}
			
			Element PayloadType = new Element("PayloadType");
			PayloadType.addContent(Integer.toString(frame.payloadType()));
			RTPPkt.addContent(PayloadType);
			
			Element Marked = new Element("Marked");
			Marked.addContent(Boolean.toString(frame.marked()));
			RTPPkt.addContent(Marked);
			
			Element SSRC = new Element("SSRC");
			SSRC.addContent(Long.toString(frame.ssrc()));
			RTPPkt.addContent(SSRC);
			
			long[] csrcArray = frame.csrcs();
			for(int i=0; i< csrcArray.length; i++) {
				Element CSRC = new Element("CSRC");
				CSRC.addContent(Long.toString(csrcArray[i]));
				RTPPkt.addContent(CSRC);
			}
			
			Element Payload = new Element("Payload");
			byte[] payload = frame.getConcatenatedData();
			StringBuffer buf = new StringBuffer();
			for(int i=0; i<payload.length && i<64; i++ ) {
				buf.append(StaticProcs.hexOfByte(payload[i]));
			}
			Payload.addContent(buf.toString());
			RTPPkt.addContent(Payload);
			
			this.packetCount++;
		}
		
		public void userEvent(int type, Participant[] participant) {
			if(type == 1) {
				this.noBye = false;
			} else {
				//Do nothing
			}
		}
		
		public int frameSize(int payloadType) {
			return 1;
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
			int rtpPortNum = -1;
			int rtcpPortNum = -1;
			int maxPacketCount = -1;
			String filename = "";
			boolean run = false;
			if (args.length == 4) {
			    try {
			    	rtpPortNum = Integer.parseInt(args[0]);
			    	rtcpPortNum = Integer.parseInt(args[1]);
			    	filename = args[2];
			    	maxPacketCount = Integer.parseInt(args[3]);
			    } catch (NumberFormatException e) {
			    	System.out.println(e.getMessage());
			    }
			    run = true;
			} else if(args.length == 0) {
				System.out.println("Syntax: ");
				System.out.println("java XmlPacketRecorder <RTP listen port> <RTCP listen port> <file to save> <max number of packets>");
				System.out.println("If \"max number of packets\" is set to something negative " +
						"the system will run until it receives a BYE message.");
				System.out.println("");
				System.out.println("Using default values for testing, will only work on a UNIX clone:");
				System.out.println("java XmlPacketRecorder 16384 16385 ~/jlibrtp_packets.xml 500");

		    	rtpPortNum = 16384;
		    	rtcpPortNum = 16385;
		    	filename =  "/home/ak/jlibrtp_packets.xml";
		    	maxPacketCount = 500;
				
				run = true;
			} else {
				System.out.println("Syntax: ");
				System.out.println("java XmlPacketRecorder <RTP listen port> <RTCP listen port> <file to save> <max number of packets>");
				System.out.println("If \"max number of packets\" is set to something negative " +
						"the system will run until it receives a BYE message.");
			}
			
			if(run) {
				XmlPacketRecorder recorder = new XmlPacketRecorder(rtpPortNum, rtcpPortNum, maxPacketCount);
				recorder.createDocument();

				System.out.println("Waiting for packets, dots denote received packets in interval of 500ms.");
				int prevCount = 0;
				while((recorder.packetCount < 0 || recorder.packetCount < recorder.maxPacketCount) 
						&& recorder.noBye) {	
					if(recorder.packetCount > prevCount)
						System.out.print(".");
					prevCount = recorder.packetCount;

					try { Thread.sleep(200); } catch (Exception e) { System.out.println("oops."); }
				}
				System.out.println();

				try { Thread.sleep(200); } catch (Exception e) { System.out.println("oops."); }

				System.out.println("Writing XML");
				try {
					XMLOutputter outputter = new XMLOutputter();
					FileWriter writer = new FileWriter(filename);
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
}
