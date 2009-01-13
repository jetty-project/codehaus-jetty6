<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>


<h1>Asynchronous Jetty+REST</h1>
<p>
This demonstration shows how the proposed servlet 3.0 asynchronous 
API (inspired by Jetty continuations) can be used to greatly 
improve web services. 
The demo calls the EBay WS API both synchronously and asynchronously,
to obtain items matching each of the keywords passed on the query
string.
</p>


<table width='100%' border=1 cellpadding=5 cellspacing=0>
<tr>
<th>Keys</th>
<th>Synchronous</th>
<th>Asynchronous</th>
</tr>

<tr>
<td><%= request.getParameter("items") %></td>
<td>
  <iframe id="f1" width='100%' height='200px' src="testSerial?items=<%= request.getParameter("items") %>"></iframe>
</td>
<td>
  <iframe id="f2" width='100%' height='200px' src="testAsync?items=<%= request.getParameter("items") %>"/></iframe>
</td>
</tr>
 

</table>
</p>

<p>
The 
<a
href="http://svn.codehaus.org/jetty/jetty/trunk/modules/examples/async-rest-example-webapp/src/main/java/org/mortbay/demo/SerialRestServlet.java">synchronous
servlet</a> makes a ws call for each keyword one by one. The thread servicing the
web request blocks, waiting for each ws response before making the next
request. The servlet thread is held for the entire elapsed time of all ws calls.

</p>
<p>

The <a
href="http://svn.codehaus.org/jetty/jetty/trunk/modules/examples/async-rest-example-webapp/src/main/java/org/mortbay/demo/AsyncRestServlet.java">
asynchronous servlet
</a> initiates an async REST call for all keywords in parallel, and
then uses the Servlet 3.0 suspend call to return the thread to the thread
pool. The async callbacks check to see if all callbacks are complete and
if so, the request is resumed.  The ws calls are done in parallel and the 
servlet thread is only held for a short time at the start and end of the elapsed time.
</p>
  
<p>Try your own search (comma-separated list):
<form method="GET" action="index.jsp">
  
  <input type="text" name="items" /> 
  <input type="submit" value="Search" />
</form>
</p>