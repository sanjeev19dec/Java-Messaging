<%@ page contentType="text/html;charset=ISO-8859-1" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title> dgServices - LDAP Password </title>
<!-- stylesheet include begin -->
<link rel="stylesheet" href="includes/uti_portal.css" type="text/css" />
<!-- stylesheet include end -->

</head>
<body>
<table cellpadding="0" cellspacing="0" class="it1c0">

<tr>
<td height="20"></td>
</tr>

<tr>
<td class="ih1"> Enter LDAP Password</td>
</tr>
</table>

<form name="pwordform" method="post" 
      action="controller?action=pword&userid=<%=request.getParameter("userid")%>">
<table border="0" align="center" cellpadding="0" cellspacing="0" class="it1c0">
  <tr>
    <td></td>
    <td><b><%=request.getAttribute("message")%></b></td>
  </tr>
  <tr>
    <td>LDAP Password:</td>
    <td><input type="password" name="password" size="10"></td>
  </tr>
</table>
<table border="0" align="center" cellpadding="0" cellspacing="0" class="it1c0">
  <tr>
    <td><input type="submit" name="p_submit" value="Submit"/></td>
  </tr>
</table>
</form>
</body>
</html>
