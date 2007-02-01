package jlibrtp;

public class CompRtcpPkt {
	private boolean rawPktCurrent = false;
	private byte[] rawPkt = null;
	private byte[][] rawRtcpPkts = null;
	private RtcpPkt[] rtcpPkts;

}
