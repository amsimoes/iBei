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
	
	<h3>Top 10 Victories:</h3>
	<c:forEach items="${statsVitorias}" var="u">
	<c:if test="${u.vitorias > 0}">
		<p style="display:inline" > <c:out value="${u.username}"/> ---- </p>
		<p style="display:inline">  <c:out value="${u.vitorias}" /> </p><br>
	</c:if>
	</c:forEach>

	<h3>Top 10 Users:</h3>
	<c:forEach items="${statsLeiloes}" var="u">
		<c:if test="${u.leiloes > 0}">
			<p style="display:inline" > <c:out value="${u.username}"/> ---- </p>
			<p style="display:inline">  <c:out value="${u.leiloes}" /> </p><br>
		</c:if>
	</c:forEach>
	
	<h3>Top 10 Auctions:</h3>
	<p>${statsLastWeek}</p>
	

	<form action="indexAdmin">
	    	<input type="submit" value="Voltar" />
	</form>

</body>
</html>
