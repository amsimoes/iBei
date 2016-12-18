<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="css/stats.css">
</head>
<body>
	
		
		<div id="p">
			<h1>Top 10 Users:</h1>
			<c:forEach items="${statsLeiloes}" var="u">
				<c:if test="${u.leiloes > 0}">
					<h2 style="display:inline" > <c:out value="${u.username}"/> ---- </h2>
					<h2 style="display:inline">  <c:out value="${u.leiloes}" /> </h2><br>
				</c:if>
			</c:forEach>
		</div>
		<div id="p">
			<h1>Top 10 Victories:</h1>
			<c:forEach items="${statsVitorias}" var="u">
			<c:if test="${u.vitorias > 0}">
				<h2 style="display:inline" > <c:out value="${u.username}"/> ---- </h2>
				<h2 style="display:inline">  <c:out value="${u.vitorias}" /> </h2><br>
			</c:if>
			</c:forEach>
		</div>
		<div id="p">
			<h1>Top 10 Auctions:</h1>
			<h2>${statsLastWeek}</h2>
		</div>
	
		<div style="margin-top: 5vh;">
			<form action="indexAdmin">
		    	<input style="width: 5vw;" class="btn" type="submit" value="Voltar" />
			</form>	
		</div>
	
</body>
</html>
