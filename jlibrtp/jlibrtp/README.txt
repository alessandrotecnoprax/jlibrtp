jlibrtp - Java RTP Library

Kepp, Arne
ak2618@columbia.edu

Janardhan, Vaishnav
vj2135@columbia.edu

Columbia University
New York, NY 10027
USA


------Abstract
jlibrtp is a library that implements the Real-Time Transport Protocol (RTP), 
a well-established standard for streaming media across IP-based networks, in Java. The purpose 
of this library is to make it easy for application developers to create applications for peer 
to peer streaming of audio, video and other data. In addition, developers will need a protocol 
to establish contact with peers, such as Session Initialization Protocol (SIP) and/or SDP.

Our library accepts any kind of binary data, handles packet parsing and reordering, maintains a 
participant database and the control connection associated with the protocol. The application 
is notified of received data through a callback-interface. The library supports IPv4, IPv6 and 
multicast. It does currently not support encryption, and should not be used in cases where 
confidentiality is important before this has been remedied.

Please refer to http://jlibrtp.org

The demonstration programs can be compiled as follows:
javac ./jlibrtpDemos/SoundSenderDemo.java jlibrtp/*.java