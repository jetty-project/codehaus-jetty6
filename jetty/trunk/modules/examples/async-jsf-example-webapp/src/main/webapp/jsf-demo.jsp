<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <title>JSF Demo</title>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <link rel="stylesheet" href="styles/jsf-demo.css" type="text/css"></link>
  </head>
  <body>
    <h1>JSF Demo</h1>
    <a href="suspend.faces">Suspend example</a> - suspends for 5s before being served.
    <br/>
    <a href="webservice.faces">Async webservice example</a>
    <br/>
    <f:view>
      <h:form id="demoForm">
      	Enter your name here:&nbsp;&nbsp;
        <h:inputText id="userName" value="#{JSFDemoBean.userName}" />
        &nbsp;&nbsp;
        <h:commandButton id="submit" action="success" value="Submit" />
      </h:form>
    </f:view>
  </body>
</html>