<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="bootstrap-3.3.7/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" type="text/css" href="css/menu.css">
</head>
<body>
	
	<h2 style="text-align:center;" ><s:property value="message"/></h2>
	
	<form action="cancelAuction.jsp">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Cancel auction" />
	</form>
	<form action="BanUser.jsp">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="Ban user" />
	</form>
	<form action="stats">
	    	<input class="btn btn-lg btn-primary btn-block" type="submit" value="See statistics" />
	</form>
	<form action="removeFacebook.jsp">
		<input class="btn btn-lg btn-primary btn-block" type="submit" value="Deassociate Facebook" />
	</form>

	<form action="logoutAction">
	    	<input style="margin-top: 8vh;" class="btn btn-lg btn-primary btn-block" type="submit" value="Logout" />
	</form>
	

</body>
</html>
