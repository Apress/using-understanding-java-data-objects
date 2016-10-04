<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%--
Copyright (c) 2002 Yankee Software.

This file is part of the JDO Learning Tools

The JDO Learning Tools is free software; you can use it, redistribute it,
and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 2 of the
License, or (at your option) any later version.

The JDO Learning Tools software is distributed in the hope that it
will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
the GNU General Public License for more details.

A copy of the GPL Version 2 is contained in LICENSE.TXT in this source
distribution.  If you cannot find LICENSE.TXT, write to the Free
Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA or visit www.fsf.org on the web.

Copyright law and the license agreement do not apply to your
understanding of the the concepts, principles, and practices embedded
in this code.  The purpose of the JDO Learning Tools to to help
advance the use and understanding of Java Data Objects, the standard
for transparent persistence for Java objects from the Java Community
Process.

Change History:

Please insert a brief record of any changes made.

Author            Date        Purpose
-----------------+----------+-----------------------------------
David Ezzio       10/01/02   Created
--%>

<%@ page import="com.ysoft.jdo.book.rental.servlet.util.*" errorPage="exception.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<jsp:useBean id="model" scope="request" class="com.ysoft.jdo.book.rental.servlet.util.ReservationModel" />
<%
// Ensure a refresh in all browsers!
response.setHeader("Pragma", "no-cache");
response.setHeader("Expires", "-1");
response.setHeader("Cache-Control", "no-cache");
%>

<html>
<head>
   <title>Using and Understanding Java Data Objects</title>
</head>

<body>

<p align="center">
<font size=5>
The Maine Lighthouse Rental
</font>
<c:set var="lighthouse" value="${model.lighthouses[param.index]}"/>
<CENTER><IMG border=4 src="/rental/images/<c:out value="${lighthouse.imageName}"/>.jpg">
<P>
<c:out value="${lighthouse.description}" />
<P>
Picture courtesy of <A HREF="http://www.lighthousegetaway.com">
Lighthouse Getaway</A>

</body>
</html>
