<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="css/model.css">
</head>
<body>
	<p>Create Auction</p>
	<s:form action="createAction" method="post">
		
		<s:textfield cssClass="input" name="code" placeholder="Code" /><br>
		
		<s:textfield cssClass="input" name="deadline" placeholder="ex: 2017-1-1 10:00:00"/><br>
		
		<s:textfield cssClass="input" name="title" placeholder="Title" /><br>
		
		<s:textfield cssClass="input" name="description" placeholder="Description" /><br>
		
		<s:textfield cssClass="input" name="amount" placeholder="Amount" /><br>
		
		<s:submit cssClass="btn"/>
	</s:form>
</body>
</html>
