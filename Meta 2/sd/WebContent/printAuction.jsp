<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/answer.css">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Hey!</title>
</head>
<body>
	<div id="auc" style="margin-left: 35vw; margin-top: 10vh;">
		<p>It works</p>
		<p>${leilao.id_leilao}</p>
		<p>${leilao.username_criador}</p>
		<p>${leilao.titulo}</p>
		
		<p>Messages: </p>
			<c:forEach items="${leilao.mensagens}" var="value">
				<c:out value="${value}" /><br>
			</c:forEach>
		<p>Bids: </p>
		<c:forEach items="${leilao.licitacoes}" var="value">
			<c:out value="${value}" /><br>
		</c:forEach>
	<div id= "back">
		<form action="index">
		    	<input class="btn" type="submit" value="Voltar" />
		</form>	
	</div>
	</div>

</body>
</html>
