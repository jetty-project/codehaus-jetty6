<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%
if (request.isInitial() || request.getAttribute("async.jsf.demo")==null)
{
    System.err.println("suspending");
    request.setAttribute("async.jsf.demo",Boolean.TRUE);
    
    request.suspend(30000);
    
    // Need to disable response as JSF does not know about suspend (yet)
    response.disable();
    
    // simulate a callback to resume
    final ServletRequest r = request;
    new Thread(){
        public void run() { try { Thread.sleep(5000); }catch(Exception e){}; r.resume();}
    }.start();
    
    return;
}
System.err.println("redispatched");

%>
<html>
  <head>
    <title>Suspend Demo</title>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
    <link rel="stylesheet" href="styles/jsf-demo.css" type="text/css"></link>
  </head>
  <body>
    <h1>Suspend Demo</h1>
    <br/>
    This is a demo.  Request initial=<%=request.isInitial()%> resumed=<%=request.isResumed()%>
  </body>
</html>