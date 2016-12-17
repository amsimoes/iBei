<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="websocket.js"></script>
<link rel="stylesheet" type="text/css" href="css/model.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
	<p>Make Bid</p>
	<s:form action="bidAction" method="post">
		<s:textfield cssClass="input" name="Id" placeholder="Auction Id"/><br>
				
		<s:textfield cssClass="input" name="amount" placeholder="Amount:" style="margin-top: 1vh;"/><br>
		
		<s:submit  cssClass="btn"/>
	</s:form>
	<!-- button type="submit" onClick="NotfBid()">Enviar Bid notificacoes</button-->
	
	<form action="index">
		    	<input class="btn" type="submit" value="Voltar" />
	</form>	
</body>
</html>
