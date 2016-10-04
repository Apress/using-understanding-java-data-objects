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

<%@ page import="java.util.*,com.ysoft.jdo.book.rental.servlet.util.*" errorPage="exception.jsp"%>
<jsp:useBean id="slowBean" scope="application" class="com.ysoft.jdo.book.rental.servlet.beans.SlowBean" />
<%
// Ensure a refresh in all browsers!
response.setHeader("Pragma", "no-cache");
response.setHeader("Expires", "-1");
response.setHeader("Cache-Control", "no-cache");
%>

<HTML>

<HEAD><TITLE>SessionLock Bean Use</TITLE>
<META content="text/html; charset=windows-1252" http-equiv=Content-Type>
</HEAD>

<BODY>
This page uses the SessionLock to serialize the threads in the session during page generation.
<P>
The first thread in the page generation is delayed for testing purposes by SlowBean after acquiring
the lock.  Subsequent threads must wait for the session lock but are not delayed by SlowBean.
<p>
This page asked for the lock at <%=(new Date().toString())%>
<% long create_time = session.getCreationTime(); %>
<P>
This session was created at <%=(new Date(create_time))%>
<P>
This session is <%=(session.isNew() ? "" : "not ")%> new.
<P>
<% long now = System.currentTimeMillis(); %>
This session was last accessed <%= (now - session.getLastAccessedTime()) / 1000.0 %> seconds ago.

<% SessionLock lock = SessionLock.getInstance(session); %>
<%-- lock endures for 20 seconds, and delay will take 10 seconds --%>
<P>
<% int approved_action = lock.lock(21, 20); %>
The action requested was 21, the action approved was <%=approved_action%>
<P>
This lock has been called <%= lock.getLockCount()%> times.
<P>
This page generation has
<%if (approved_action == 21) slowBean.delay(10); else out.write("<i>not</i> "); %>
been delayed.
<p>
This page completed generation at <%=(new Date().toString())%>
<%lock.unlock();%>
</BODY>

</HTML>

