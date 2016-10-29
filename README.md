# iBei
[Java] Reverse Auctioning Project for Distributed Systems course

# Compilation
Inside src folder:

Compiling java files to create .class files:
$ javac iBei/aux/\*.java
$ javac iBei/rmi/\*.java
$ javac iBei/server/\*.java
$ javac iBei/admin/\*.java

Creating jar files using .class generated:
$ jar cvfm dataserver.jar manifest_rmi.mf .
$ jar cvfm server.jar manifest_tcpserver.mf .
$ jar cvfm admin.jar manifest_admin.mf .

# Running

RMI Server 
Usage: $ java -jar dataserver.jar \<rmi host ip> \<rmi host port>

TCP Server
Usage: $ java -jar server.jar \<localport> \<RMI host ip> \<RMI host port>

TCP Client
Usage: $ java TCPClient \<TCP server ip> \<TCP server port>

Admin Client
Usage: $ java -jar admin.jar \<RMI host ip> \<RMI host port>


