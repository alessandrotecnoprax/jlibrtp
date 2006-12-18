jlibrtp - Java RTP Library

Janardhan, Vaishnav
vj2135@columbia.edu

Kepp, Arne
ak2618@columbia.edu

Columbia University
New York, NY 10027
USA

------Abstract
We have created a library that partially implements the Real-Time Transport Protocol (RTP), 
a well-established standard for streaming media across IP-based networks, in Java. The purpose 
of this library is to make it easy for application developers to create applications for peer 
to peer streaming of audio, video and other data. In addition, developers will need a protocol 
to establish contact with peers, such as Session Initialization Protocol (SIP). 

Our library accepts any kind of binary data, handles packet parsing and reordering, maintains a 
participant database and the control connection associated with the protocol. The application 
is notified of received data through a callback-interface. The library supports IPv4, IPv6 and 
multicast. It does currently not support encryption, and should not be used in cases where 
confidentiality is important.

Please refer to http://metro-north.cs.columbia.edu:8080/display/~hgs/jlibrtp+-+Java+RTP+Library for details.

The demonstration programs can be compiled as follows:
javac -Xlint:unchecked senderDemo/SenderDemo.java jlibrtp/*.java
javac -Xlint:unchecked senderDemo/SenderDemo.java jlibrtp/*.java