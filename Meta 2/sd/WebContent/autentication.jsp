<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/signin.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

</head>
<body class="form-signin" >
	<div id="login">
		<s:form  method="post" action="Login">
			<h2 class="form-signin-heading">Login</h2>
			<s:textfield cssClass="form-control" name="username" placeholder="Username" /><br>
			<s:textfield cssClass="form-control" name="password" placeholder="Password" /><br>
			<s:submit value="Login"/>
		</s:form>
	</div>
	<div id="register">
		<s:form  method="post" action="Register">
			<h2 class="form-signin-heading">Sign up</h2>
			<s:textfield cssClass="form-control" name="username" placeholder="Username" /><br>
			<s:textfield cssClass="form-control" name="password" placeholder="Password" /><br>
			<s:submit value="Register"/>
		</s:form>
	</div>
	<h2><s:property value="print"/></h2>
	
</body>
</html>
