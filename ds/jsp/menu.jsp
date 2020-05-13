<%@ page contentType="text/html;charset=ISO-8859-1" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title> dgServices - Directory Services Menu </title>
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
<td class="ih1">Menu</td>
</tr>
</table>

<p><a href="controller?action=grpsearch&grpaction=grpalloc&userid=<%=request.getParameter("userid")%>">Group Allocations</a>
<p><a href="controller?action=grpsearch&grpaction=grpdealloc&userid=<%=request.getParameter("userid")%>">Group Deallocations</a>
</p>
</body>
</html>
