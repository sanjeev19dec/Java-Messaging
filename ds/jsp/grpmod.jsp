<%@ include file="methods.jsp"%>
<%@ page import="uti.nextgen.ds.*,java.util.Vector" contentType="text/html;charset=ISO-8859-1" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title> dgServices - Group Modification </title>
<!-- stylesheet include begin -->
<link rel="stylesheet" href="includes/uti_portal.css" type="text/css" />
<!-- stylesheet include end -->

<script LANGUAGE="JavaScript" SRC="js/OptionTransfer.js">
  document.write("OptionTransfer PROBLEM!");
</script>
<script LANGUAGE="JavaScript" SRC="js/selectbox.js">
  document.write("selectbox PROBLEM!");
</script>
<script LANGUAGE="JavaScript">
  var opt = new OptionTransfer("leftList","rightList");
  opt.setAutoSort(true);
  opt.setDelimiter("|");
  opt.saveRemovedLeftOptions("removedLeft");
  opt.saveRemovedRightOptions("removedRight");
  opt.saveAddedLeftOptions("addedLeft");
  opt.saveAddedRightOptions("addedRight");
  opt.saveNewLeftOptions("newLeft");
  opt.saveNewRightOptions("newRight");
</script>
</head>

<%
   String leftValues = request.getParameter("newLeft");
   String rightValues = request.getParameter("newRight");
   String groupName = request.getParameter("groupName");

   if(leftValues == null)
   {
     StringBuffer leftBuf = new StringBuffer();
   
     DsMessage msgArray[] = (DsMessage[])request.getAttribute("UserList"); 
     leftBuf.append(createOptions(msgArray,"dn"));
   
     msgArray = (DsMessage[])request.getAttribute("GroupList");
     leftBuf.append(createOptions(msgArray,"dn"));
   
     leftValues = leftBuf.toString();
   }
   else
   {
     String tmpStr = createOptions(leftValues);
     leftValues = tmpStr;
   }

   if(rightValues == null)
   { 
     DsMessage modGroup = (DsMessage)request.getAttribute("ModGroup");
     groupName = (String)modGroup.getAttributeValue("cn");

     Vector members = (Vector)modGroup.getAttributeValue("uniquemember");

     rightValues = createOptions(members); 
   }
   else
   {
     String tmpStr = createOptions(rightValues);
     rightValues = tmpStr;
   }
%>

<body onLoad="opt.init(document.grpalloc)">
<form name="grpalloc" method="post" 
      action="controller?action=<%=request.getParameter("action")%>&userid=<%=request.getParameter("userid")%>">
<table border="0" align="center" cellpadding="0" cellspacing="0" class="it1c0">
  <tr>
    <td></td>
    <td><b><%=request.getAttribute("message")%></b></td>
  </tr>
  <tr>
    <td><select name="leftList" MULTIPLE SIZE=10 
                onDblClick="opt.TransferRight()">
        <%=leftValues%>
       </select>
    </td>
    <td VALIGN=MIDDLE ALIGN=CENTER>
      <input TYPE="button" NAME="right" VALUE="&gt;&gt;" 
             ONCLICK="opt.transferRight()"><br><br>
      <input TYPE="button" NAME="right" VALUE="All &gt;&gt;" 
             ONCLICK="opt.transferAllRight()"><br><br>
      <input TYPE="button" NAME="left" VALUE="&lt;&lt;" 
             ONCLICK="opt.transferLeft()"><br><br>
      <input TYPE="button" NAME="left" VALUE="All &lt;&lt;" 
             ONCLICK="opt.transferAllLeft()">
    </td>
    <td><select name="rightList" MULTIPLE SIZE=10 
                onDblClick="opt.TransferLeft()">
        <%=rightValues%>
       </select>
    </td>
  </tr>
</table>

<input TYPE="hidden" NAME="groupName" VALUE="<%=groupName%>">
<input TYPE="hidden" NAME="removedLeft" VALUE="" SIZE=70>
<input TYPE="hidden" NAME="removedRight" VALUE="" SIZE=70>
<input TYPE="hidden" NAME="addedLeft" VALUE="" SIZE=70>
<input TYPE="hidden" NAME="addedRight" VALUE="" SIZE=70>
<input TYPE="hidden" NAME="newLeft" VALUE="" SIZE=70>
<input TYPE="hidden" NAME="newRight" VALUE="" SIZE=70>

<table border="0" align="center" cellpadding="0" cellspacing="0" class="it1c0">
  <tr>
    <td><input type="submit" name="p_submit" value="Update"/></td>
  </tr>
</table>
</form>
</body>
</html>
