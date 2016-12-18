<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/answer.css">
<script type="text/javascript" src="websocket.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
	<div id="c" >
		<p>Users in this page</p>
		<p id="count"></p>
	</div>
	<div id="auc" style="margin-left: 35vw; margin-top: 10vh;">
				
		<h2 style="display:inline"> Auction Id: </h2>
		<h3 style="display:inline"><c:out value="${leilao.id_leilao}" /> </h3>
		<h3 style="display:inline"> <br><br>Auction Owner: </h3>
		<c:out value="${leilao.username_criador}"/>
		<h3 style="display:inline"> <br><br>Auction title: </h3>
		<c:out value="${leilao.titulo}"/>
		<h3>Messages: </h3>
			<c:forEach items="${leilao.mensagens}" var="value">
				<c:out value="${value}" /><br>
			</c:forEach>
			<h3>Bids: </h3>
			<c:forEach items="${leilao.licitacoes}" var="value">
				<c:out value="${value}" /><br>
			</c:forEach> <br><br>
	<div id= "back">
		<form action="index">
		    	<input class="btn" type="submit" value="Voltar" />
		</form>	
	</div>
	</div>

</body>
</html>