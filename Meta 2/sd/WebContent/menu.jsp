<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="websocket.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="bootstrap-3.3.7/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" type="text/css" href="css/menu.css">


</head>
<body>
	
	<h2 style="text-align:center;" ><s:property value="message"/></h2>
	<div style=" width: 15vw; border: 1px solid black; border-radius: 5px; position: absolute; margin-left: 35vw; margin-top: 7vh;" >
		<p id="online_users" style="text-align: center"></p>
	</div>
	<form action="myAuctions">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="My auctions" />
	</form>
	<form action="detailAuction.jsp">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Detail Auction" />
	</form>
	<form action="editAuction.jsp">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Edit Auction" />
	</form>
	<form action="searchAuction.jsp">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Search Auction" />
	</form>
	<form action="createAuction.jsp">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Create Auction" />
	</form>
	<form action="bidAuction.jsp">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Make bid" />
	</form>
	<form action="msgAuction.jsp">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Write Message" />
	</form>
	<form action="logoutAction">
	    	<input style="margin-top: 8vh;" class="btn btn-lg btn-primary btn-block" type="submit" value="Logout" />
	</form>
	
	
	
</body>
</html>
