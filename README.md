# iBei
[Java] Reverse Auctioning Project for Distributed Systems course

# Running:

RMI Server 
Usage: $ java -jar dataserver <rmi host ip> <rmi host port>

TCP Server
Usage: $ java -jar server <localport> <RMI host ip> <RMI host port>

TCP Client
Usage: $ java TCPClient <TCP server ip> <TCP server port>

Admin Client
Usage: $ java -jar admin <RMI host ip> <RMI host port>

# Compilation

jar cvfm dataserver.jar manifest_rmi.mf .
jar cvfm server.jar manifest_tcpserver.mf .
jar cvfm admin.jar manifest_admin.mf .
