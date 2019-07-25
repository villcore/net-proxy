<%@ page import="java.util.Date" %>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>Net-Proxy客户端管理</title>
</head>

<body>
<%--<H1 align="center">Net-Proxy客户端管理</H1>--%>
<div>
    <span style="font-weight: bold; color: red">全局代理:
        <c:choose>
            <c:when test="${globalProxy}">
                开
            </c:when>
            <c:otherwise>
                关
            </c:otherwise>
        </c:choose>
    </span>

    <a href="${pageContext.request.contextPath}/global_proxy?globalProxy=true"
       style="font-weight: bold; margin-left: 50px">打开</a>
    <a href="${pageContext.request.contextPath}/global_proxy?globalProxy=false" style="font-weight: bold">关闭</a>
    <span>${localChannels}/${remoteChannels}/${openChannels}</span>
</div>
<br/>
<table border="1px" width="100%">
    <tr align="center">
        <td style="font-weight: bold">地址</td>
        <td style="font-weight: bold">是否直连</td>
        <td style="font-weight: bold">打开连接</td>
        <td style="font-weight: bold">操作</td>
    </tr>
    <c:forEach items="${accessablityMap}" var="entry">
        <tr <c:if test="not ${entry.accessable}">style='font-weight: bold; color:royalblue'</c:if>>
            <td>${entry.address}</td>
            <td>${entry.accessable}</td>
            <td>${entry.count}</td>
            <td>
                <a href="${pageContext.request.contextPath}/proxy?address=${entry.address}">代理</a>
                <a href="${pageContext.request.contextPath}/local?address=${entry.address}">本地</a>
                <a href="${pageContext.request.contextPath}/remove?address=${entry.address}">删除</a>
            </td>
        </tr>
    </c:forEach>

</table>

<script language="JavaScript">
    function Refresh() {
        window.location.reload();
    }

    setTimeout('Refresh()', 1000); //1秒刷新一次
</script>
</body>
</html>