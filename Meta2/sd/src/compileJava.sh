#! /bin/bash

javac -cp "lib/*" iBei/aux/*.java
javac iBei/rmi/*.java
javac iBei/server/*.java

jar cfm dataserver.jar manifests/manifest_rmi.mf iBei
jar cfm server.jar manifests/manifest_tcpserver.mf iBei

