<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="webSocket.js"></script>
<link rel="stylesheet" type="text/css" href="css/answer.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
	<c:forEach items="${leiloes}" var="auction" varStatus="status">
		<div id="auc">
			<h2 style="display:inline"> Auction Id: </h2>
			<h3 style="display:inline"><c:out value="${auction.id_leilao}" /> </h3>
			<h3 style="display:inline"> <br><br>Auction Owner: </h3>
			<c:out value="${auction.username_criador}"/>
			<h3>Messages: </h3>
			<c:forEach items="${auction.mensagens}" var="value">
				<c:out value="${value}" /><br>
			</c:forEach>
			<h3>Bids: </h3>
			<c:forEach items="${auction.licitacoes}" var="value">
				<c:out value="${value}" /><br>
			</c:forEach> <br><br>
		</div>
	</c:forEach>
	<div>
		<form action="index">
	    	<input type="submit" value="Voltar" />
		</form>	
	</div>
</body>

</html>