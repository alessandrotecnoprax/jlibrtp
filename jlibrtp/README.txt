jlibrtp - Java RTP Library

Kepp, Arne
ak2618@columbia.edu

Janardhan, Vaishnav
vj2135@columbia.edu

Columbia University
New York, NY 10027
USA

------Abstract
We have created a library that partially implements the Real Time Protocol (RTP), a well-established standard for streaming media across IP-based networks, in Java. The purpose of this library is to make it easy for application developers to create applications for peer to peer streaming of audio, video and other data. In addition, developers will need a protocol to establish contact with peers, such as Session Initialization Protocol (SIP). Our library accepts any kind of binary data, handles packet parsing and reordering, maintains a participant database and the control connection associated with the protocol. The application is notified of received data through a callback-interface. The library supports IPv4, IPv6 and multicast. It does currently not support encryption, and should not be used in cases where confidentiality is important.

------Introduction
jlibrtp partially implements the Real Time Protocol (RTP, RFC 3550)[1]. It is an alternative to the RTP stack provided by Sun Microsystems (R) in the Java Media Framework (JMF)[2], the difference is that we attempt to provide application developers with a much simpler application programming interface (API). 

To use jlibrtp, developers must instatiate a copy of the RTPSession class and provide a class that implements the callback interface RTPAppIntf. The RTPSession object is a container for the datastructures required by the library, and starts the threads used for receiving and parsing packets. The program can use simple methods to add peers, participants that sende or receive packets, to the session. The callback interface is called whenever the stack has data ready for the application, and it is up to the developer to decide what to do with the binary data.

Our code is licensed under the GNU Lesser General Public License (LGPL)[3], developers are therefore free to change functionality or provide a richer API if the current implementation does not meet their needs.

...... links to subsections .....

------Related Work
The Java Media Framework provides much of the same functionality as jlibrtp, but requires the application developer to do considerably more work and cannot easily be modified to accomodate special requirements. 

JRTP[4] is a framework for media distribution that also includes RTP. However, it is a comprehensive program for the distribution of content, such as television, and the RTP component is not designed to be used separately. 

java.net.rtp[5] is a relatively old application specialized for a whiteboard application. It only supports multicast, which currently severly limits its usefulness in common VoIP applications, and lacks the datastructures to handle separate streams.

There are several RTP stacks available in C and C++, including GNU Telephony's ccRTP[6].


------Architecture
RTPSession

RTP

RTCP

Threads

------Testing
---Interoperability
The library has been validated in three ways. First, we have looped packets through the library to verify that the parsing is correct. 

---Performance
We tested the library by streaming raw audio (44.1kHz, 16 bit, stereo) across the loopback interface, i.e. the same instance of the library was both sending and receiving. The test system (2.13 GHz, Linux 2.6.17, Java 1.5.0) had no problems handling the 2.8 Mbps. While packet parsing is expensive in Java, due to lack of pointers and unsigned integers, this would be more than sufficient to stream video.

---Security
Java inherently provides some protection against buffer overflows and other common problems. Additionally, packet buffer size is automatically limited to 2000 octets. Developers can choose whether packets from unknown sources should be returned to the application or discarded.

Our library does not implement encryption according to the Secure RTP (SRTP, RFC)[] specifications, and therefore provides no confidentiality. All streams can be played by anyone able to intercept the packets.

------Example application
The example below


------Task List
Code




References

 
