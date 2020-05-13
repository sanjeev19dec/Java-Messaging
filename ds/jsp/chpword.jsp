<%@ page contentType="text/html;charset=ISO-8859-1" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title> dgServices - Change User Password </title>
<!-- stylesheet include begin -->
<link rel="stylesheet" href="includes/uti_portal.css" type="text/css" />
<!-- stylesheet include end -->

<script type="text/javascript">
  function submitForm()
  {
    if (document.password.confirmpassword.value != document.password.newpassword.value)
    {
      alert("Passwords does not match");
 	 return false;
    }

    document.password.submit();
  }
</script>
</head>

<body>
<table cellpadding="0" cellspacing="0" class="it1c0">

<tr>
<td height="20"></td>
</tr>

<tr>
<td class="ih1"> Change Own Password</td>
</tr>
</table>

<form name="password" method="post" 
      action="controller?action=chpword&userid=<%=request.getParameter("userid")%>">
<table border="0" align="center" cellpadding="0" cellspacing="0" class="it1c0">
  <tr>
    <td>
    </td>
    <td><b><%=request.getAttribute("message")%></b>
    </td>
  </tr>
  <tr>
    <td>Old Password
    </td>
    <td><input type="password" name="password" size="10">
    </td>
  </tr>
  <tr>
    <td>New Password
    </td>
    <td><input type="password" name="newpassword" size="10">
    </td>
  </tr>
  <tr>
    <td>Retype Password
    </td>
    <td><input type="password" name="confirmpassword" size="10">
    </td>
  </tr>
</table>
<table border="0" align="center" cellpadding="0" cellspacing="0" class="it1c0">
  <tr>
    <td><input type="button" name="p_submit" value="Update" class="ibut" onclick="javascript:submitForm();">
        <input type="reset" name="p_submit" value="Reset" class="ibut">
    </td>
  </tr>
</table>
</form>
</body>
</html>
