# iBei
[Java] Reverse Auctioning Project for Distributed Systems course

# Requirements
1.1 Java 7+

1.2 Apache Tomcat 8.5

1.3 MySQL JDBC

1.4 MySQL 5.7 


# Compilation
Below commands must be run inside src folder:

Compiling and creating jar files:  
* `./compileJava.sh`  

Compiling TCPClient:
* `javac TCPClient.java`  

Creating Database and tables:
* `$ mysql −u <User> −p`  
* `mysql> create database <DB_name>;`
* `mysql> use <DB_name>;`
* `mysql> source createTables.sql;`


# Running

RMI Server   
Usage: `$ java -jar dataserver.jar <rmi host ip> <rmi host port> <DataBase host> <DataBase name>`

TCP Server  
Usage: `$ java -jar server.jar <localport> <RMI host ip> <RMI host port>`

TCP Client  
Usage: `$ java TCPClient <TCP server ip> <TCP server port>`

Admin Client  
Usage: `$ java -jar admin.jar <RMI host ip> <RMI host port>`

WebServer  
Usage: Copy the iBei.war file into Tomcat’s webapps directory, and startup the Apache Tomcat 


