<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
	<p>Logged with success!</p>
	
	<a href="cancelAuction.jsp">Cancel auction<br></a>
	<a href="BanUser.jsp">Ban User<br></a>
	<a href="<s:url action="stats" />">See statistics<br></a>
	<a href="<s:url action="logoutAction" />">Logout</a>
	<h2 style="text-align:center;" ><s:property value="message"/></h2>

</body>
</html>
