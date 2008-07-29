<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%



%>
<html>
  <head>
    <title>Async JSF ebay Demo</title>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <link rel="stylesheet" href="styles/jsf-demo.css" type="text/css"></link>
  </head>
  <body>
    <h1>Async JSF Ebay Demo</h1>
    <br/>
    <f:view>
      <h:form id="demoForm">
        Items from ebay for &nbsp;<h:outputText value="#{JSFDemoBean.itemName}" />:<br/>
        <%
           java.util.List list=(java.util.List)request.getAttribute("items");
           for (Object o : list)
           {
               java.util.Map m = (java.util.Map)o;
        %>
               Item: <%=m.get("ItemID")%>&nbsp;<%=m.get("Title")%><br/>
        <%
           }
        %>
        <br/>
        <%=request.getAttribute("message")%>
        <br/>
        <h:commandButton id="back" action="success" value="Back"/>
      </h:form>
    </f:view>
  </body>
</html>