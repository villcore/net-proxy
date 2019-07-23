<%@ page import="java.util.Date" %>
<%@ page language="java" pageEncoding="UTF-8"%>
<%@page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>Net-Proxy客户端管理</title>
</head>

<body>
<table>
    <tr>
        <td>地址</td>
        <td>是否直连</td>
        <td>操作</td>
    </tr>

    <%
        System.out.println(new Date());
    %>
    <c:out value="${accessablityMap}" />
    <c:forEach items="${pageScope.accessablityMap}" var="accessablity">
        <tr>
            <td>地址</td>
            <td>是否直连</td>
            <td>操作</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>