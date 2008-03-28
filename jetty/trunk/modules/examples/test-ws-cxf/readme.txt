There are now two servlets in there, an Async and a Serial version of the same thing.

To give it a try run:

> mvn jetty:run

Now open up a browser and two tabs, in one tab go to

http://localhost:9090/testAsync?items=mouse,cheese,beer,mac,coffee,sweet,pony,computer,coat,phone,fridge,chair

and in the other go to:

http://localhost:9090/testSerial?items=mouse,cheese,beer,mac,coffee,sweet,pony,computer,coat,phone,fridge,chair

The first call is the worse as it initializes a mess of cxf things but subsequent calls show what is going on well so
just disregard the output from the first call.

I have it setup so it gets 100 results for each item.  In the console you can see the serial one outputting how long
each request is taking...all the while tying up a servlet thread since it is all in the doPost() method.  The Async
one fires off all of the requests at once and then suspends, waking up after 1000ms to check on things.  On this
connection I am seeing each request taking about 800-900 ms so the serial one is scaling basically linearly which
makes sense, its aggregating these things one right after the other so for about 8 items I am seeing a
total time: 9261ms.  However with the async one I am seeing it in just over 2000ms where the second awakening is
finding everything complete and then returning.
