<%@ page import="uti.nextgen.ds.*,java.util.*"%>
<%!
  public String createOptions(DsMessage msgArray[],String attrName)
  {
    StringBuffer buffer = new StringBuffer();

    for(int i = 0; i < msgArray.length; i++)
    {
      DsMessage msg = msgArray[i];
      buffer.append("<option>");
      buffer.append((String)msg.getAttributeValue(attrName)+"</option>\n");
    }

    return buffer.toString();
  }


  public String createOptions(Vector values)
  {
    StringBuffer buffer = new StringBuffer();

    for(int i = 0; i < values.size(); i++)
    {
      buffer.append("<option>");
      buffer.append((String)values.elementAt(i));
      buffer.append("</option>\n");
    }

    return buffer.toString();
  } 


  public String createOptions(String delimitedString)
  {
    StringBuffer buffer = new StringBuffer();
    StringTokenizer tk = new StringTokenizer(delimitedString,"|");

    while(tk.hasMoreTokens())
    {
      buffer.append("<option>");
      buffer.append(tk.nextToken());
      buffer.append("</option>\n");
    }

    return buffer.toString();
  } 
%>
