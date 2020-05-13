<%@ include file="methods.jsp"%>
<%@ page import="uti.nextgen.ds.*" contentType="text/html;charset=ISO-8859-1" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title> dgServices - Group Search </title>
<!-- stylesheet include begin -->
<link rel="stylesheet" href="includes/uti_portal.css" type="text/css" />
<!-- stylesheet include end -->

</head>

<%
   DsMessage msgArray[] = (DsMessage[])request.getAttribute("GroupList");
   String grpList = createOptions(msgArray,"cn");
%>

<body>
<table cellpadding="0" cellspacing="0" class="it1c0">

<tr>
<td height="20"></td>
</tr>

<tr>
<td class="ih1"> Search for User and Group Entries</td>
</tr>
</table>

<form name="grpsearch" method="get">
<input name="action" 
       type="hidden" value="<%=request.getParameter("grpaction")%>">
<input name="userid" 
       type="hidden" value="<%=request.getParameter("userid")%>">

<table border="0" align="center" cellpadding="0" cellspacing="0" class="it1c0">
  <tr>
    <td></td>
    <td><b><%=request.getAttribute("message")%></b></td>
  </tr>
  <tr>
    <td>Search Criteria</td>
    </td>
  </tr>
  <tr>
    <td>Search for users: (uid)</td>
    <td><input type="text" name="uidFilter" size="10"></td>
  </tr>
  <tr>
    <td>Or, search for users: (cn)</td>
    <td><input type="text" name="cnFilter" size="10">
    </td>
  </tr>
  <tr>
    <td>Choos a group to edit: </td>
    <td><select name="groupName">
        <%=grpList%>
        </select>
    </td>
  </tr>
</table>
<table border="0" align="center" cellpadding="0" cellspacing="0" class="it1c0">
  <tr>
    <td><input type="submit" name="p_submit" value="Update"/></td>
  </tr>
</table>
</form>
</body>
</html>
