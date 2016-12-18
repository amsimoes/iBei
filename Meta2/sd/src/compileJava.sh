#! /bin/bash

javac -cp "lib/*" iBei/aux/*.java
javac iBei/rmi/*.java

jar cfm dataserver.jar manifests/manifest_rmi.mf iBei

