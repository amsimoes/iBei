#! /bin/bash

javac -cp "lib/*" iBei/aux/*.java
javac iBei/rmi/*.java
javac iBei/server/*.java
#javac iBei/admin/*.java

jar cfm dataserver.jar manifest_rmi.mf iBei
jar cfm server.jar manifest_tcpserver.mf iBei
#jar cfm admin.jar manifest_admin.mf iBei
