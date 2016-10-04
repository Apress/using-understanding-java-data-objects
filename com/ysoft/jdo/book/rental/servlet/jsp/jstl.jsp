<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<%@ page errorPage="exception.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%
// Ensure a refresh in all browsers!
response.setHeader("Pragma", "no-cache");
response.setHeader("Expires", "-1");
response.setHeader("Cache-Control", "no-cache");
%>

<HTML>

<HEAD><TITLE>Test JSTL</TITLE>
<META content="text/html; charset=windows-1252" http-equiv=Content-Type>
</HEAD>

<BODY>
This page tests the Java ServerPages Standard Tag Library
<P>
<c:out value="Hi There" />
<P>
</BODY>

</HTML>

