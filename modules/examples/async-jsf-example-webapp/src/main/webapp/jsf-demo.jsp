<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <title>Async JSF Demo</title>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <link rel="stylesheet" href="styles/jsf-demo.css" type="text/css"></link>
  </head>
  <body>
    <h1>Async JSF Demo</h1>
    
    <h2>Suspend during rendering</h2>
    <a href="suspend.faces">Suspend example</a> - Does a suspends in JSP for 5s.
    <br/>
    
    <h2>Suspend during application action</h2>
    <f:view>
      <h:form id="demoForm">
      	Search Ebay for item:&nbsp;&nbsp;
        <h:inputText id="itemName" value="#{JSFDemoBean.itemName}" />
        &nbsp;&nbsp;
        <h:commandButton id="submit" action="#{JSFDemoBean.searchEbay}" value="Search" />
      </h:form>
    </f:view>
  </body>
</html>