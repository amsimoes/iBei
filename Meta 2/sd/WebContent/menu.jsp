<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<script type="text/javascript" src="webSocket.js"></script>
</head>
<body>
	<p>Logged with success!</p>
	
	<a href="<s:url action="myAuctions" />">My auctions<br></a>
	<a href="detailAuction.jsp">Detail auction<br></a>
	<a href="editAuction.jsp">Edit Auction<br></a>
	<a href="searchAuction.jsp">Search Auction<br></a>
    <a href="createAuction.jsp">Create Auction<br></a>
	<a href="bidAuction.jsp">Make Bid<br></a>
	<a href="msgAuction.jsp">Write message<br></a>
	<a href="<s:url action="logoutAction" />">Logout</a>

</body>
</html>
