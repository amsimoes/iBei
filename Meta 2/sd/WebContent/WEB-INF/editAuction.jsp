
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Hey!</title>
</head>
<body>
	<s:form action="editAction" method="post">
		<s:text name="Id do leilao:" />
		<s:textfield name="Id" /><br>
		
		<s:text name="Deadline:" />
		<s:textfield name="deadline" placeholder="ex: 2017-1-1 10:00:00"/><br>
		
		<s:text name="Title:" />
		<s:textfield name="title" /><br>
		
		<s:text name="Description:" />
		<s:textfield name="description" /><br>
		
		<s:submit />
	</s:form>
</body>
</html>