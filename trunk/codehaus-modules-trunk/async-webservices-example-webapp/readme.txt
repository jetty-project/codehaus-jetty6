There are now two servlets in there, an Async and a Serial version of the same 
thing.

To give it a try run:

  mvn jetty:run

It is a bit slow to start as the CXF framework takes some time to initialize.
Once it is started, open up a browser and surf to 

http://localhost:9090/
