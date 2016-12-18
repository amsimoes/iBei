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
	<div id = "box">
		<p id="online_users" style="text-align: center"></p>
	</div>
	<form action="myAuctions">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="My auctions" />
	</form>
	<form action="inputDetail">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Detail Auction" />
	</form>
	<form action="inputEdit">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Edit Auction" />
	</form>
	<form action="inputSearch">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Search Auction" />
	</form>
	<form action="inputCreate">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Create Auction" />
	</form>
	<form action="inputBid">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Make bid" />
	</form>
	<form action="inputMessage">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Write Message" />
	</form>
	<a href="https://www.facebook.com/v2.8/dialog/oauth?client_id=1883105131924636&redirect_uri=http://localhost:8080/callback.jsp&response_type=code&scope=publish_actions">
		<input class="btn btn-lg btn-primary btn-block" type="submit" value="Associate Facebook" />
	</a>
	<form action="logoutAction">
	    	<input style="margin-top: 8vh;" class="btn btn-lg btn-primary btn-block" type="submit" value="Logout" />
	</form>
	

</body>
</html>
