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
<%--
// Ensure a refresh in all browsers!
response.setHeader("Pragma", "no-cache");
response.setHeader("Expires", "-1");
response.setHeader("Cache-Control", "no-cache");
--%>

<html>
<head>
   <title>Using and Understanding Java Data Objects</title>
</head>

<body>

<p align="center">
<font size=5>
The Maine Lighthouse Rental
</font>
<p>
&nbsp;
<P>

<%-- customer name form --%>
<form action="/rental/controller/customer" method="POST">
<input type="submit" value="change customer" >
<input type="text" name="<%=ReservationServletConstants.CUSTOMER_PARAM%>" value="<c:out value="${model.customerName}"/>" size="10" maxlength="40" >
</form>
<p>

<%-- view selection form --%>
<form action="/rental/controller/view" method="GET">
<table>
   <tr>
      <td><input type="submit" value="change view" ></td>
      <td>
         <INPUT name="view" type=radio value="<%=ReservationServletConstants.VIEW_AVAILABLE_STRING%>"
            <c:if test="${model.viewAvailable}">CHECKED</c:if> >Available<br>
         <INPUT name="view" type=radio value="<%=ReservationServletConstants.VIEW_CUSTOMER_STRING%>"
            <c:if test="${model.viewCustomer}">CHECKED</c:if>
            <c:if test="${not model.customerKnown}">DISABLED</c:if>  >
               Customer's Reservations<br>
         <INPUT name="view" type=radio value="<%=ReservationServletConstants.VIEW_BOTH_STRING%>"
            <c:if test="${model.viewBoth}">CHECKED</c:if>
            <c:if test="${not model.customerKnown}">DISABLED</c:if>  >
               Both
      </td></tr>
</table>
</form>
<p>

<%-- Rental reservation form --%>
<form action="/rental/controller/reservations" METHOD="POST">
<table border="4" cellpadding="8">
   <TR>
      <TH align="center">Starting Week of</TH>
      <c:forEach var="house" items="${model.lighthouses}" varStatus="status">
         <TH ALIGN="center">
            <A HREF="/rental/controller/lighthouse?index=<c:out value="${status.index}"/>" >
            <c:out value="${house.name}"/>
            </A>
         </TH>
         <%--
         <TH ALIGN="center">
            Price
         </TH>
         --%>
      </c:forEach>
   </TR>
   <%-- One row for each week that rentals are available --%>
   <c:forEach var="row" items="${model.modelRows}" >
      <TR>
         <td align="center">
            <c:out value="${row.weekString}"/>
         </td>
         <c:forEach var="node" items="${row.nodes}" >
            <TD><div align="left">
               <c:choose>
                  <c:when test="${node.modifiable}" >
                     reserve
                     <INPUT name="<%=ReservationServletConstants.RESERVATION_PARAM%>"
                     <%-- TYPE="CHECKBOX" style="HEIGHT: 20px; WIDTH: 20px" --%>
                     TYPE="CHECKBOX"
                     value="<c:out value="${node.id}"/>"
                     <c:if test="${not node.available}">CHECKED</c:if>
                     <c:if test="${not model.customerKnown}">DISABLED</c:if>  >
                     <c:out value="${node.priceString}" />
                  </c:when>
                  <c:otherwise>&nbsp;</c:otherwise>
               </c:choose>
            </DIV></TD>
         </c:forEach>
      </TR>
   </c:forEach>
</TABLE>
<p>
<INPUT TYPE="HIDDEN" NAME="<%=ReservationServletConstants.STEP_PARAM%>"
      VALUE="<c:out value="${model.step}" />" >
<c:if test="${model.numRows > 0 and model.customerKnown}" >
<input type="submit" value="submit reservation changes" >
</c:if>
</form>

</body>
</html>
