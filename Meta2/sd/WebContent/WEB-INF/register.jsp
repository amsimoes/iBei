<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/signin.css">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body class="form-signin" >
	<s:form  action="Register">
		<h2 class="form-signin-heading">Please sign in</h2>
		<s:textfield cssClass="form-control" name="username" placeholder="Username" /><br>
		<s:textfield cssClass="form-control" name="password" placeholder="Password" /><br>
		<s:submit cssClass="btn btn-lg btn-primary btn-block" type="submit" action="Register" />
	</s:form>

</body>
</html>