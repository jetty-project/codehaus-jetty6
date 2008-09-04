//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.cometd.client;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.cometd.Bayeux;
import org.cometd.Client;
import org.cometd.Listener;
import org.cometd.Message;
import org.cometd.MessageListener;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.thread.QueuedThreadPool;
import org.mortbay.util.ajax.JSON;


public class BayeuxLoadGenerator
{
    final static String __DOTS="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-=+!@#$%^&*>";
    
    SecureRandom _random= new SecureRandom();
    HttpClient http;
    InetSocketAddress address;
    ArrayList<BayeuxClient> clients=new ArrayList<BayeuxClient>();
  
    long _targetLatency;
    long _minLatency;
    long _maxLatency;
    long _totalLatency;
    long _got;
    long _over;
    AtomicInteger _subscribed = new AtomicInteger();
    
    public BayeuxLoadGenerator() throws Exception
    {
        http=new HttpClient();
        
        http.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        // http.setConnectorType(HttpClient.CONNECTOR_SOCKET);
        http.setMaxConnectionsPerAddress(20000);
        
        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMaxThreads(500);
        pool.setDaemon(true);
        http.setThreadPool(pool);
        http.start();
       
    }
    
    
    public void generateLoad() throws Exception
    {
        LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));

        System.err.print("server[localhost]: ");
        String t = in.readLine().trim();
        if (t.length()==0)
            t="localhost";
        String host=t;
        
        System.err.print("port[8080]: ");
        t = in.readLine().trim();
        if (t.length()==0)
            t="8080";
        int port = Integer.parseInt(t);

        System.err.print("context[/cometd]: ");
        t = in.readLine().trim();
        if (t.length()==0)
            t="/cometd";
        String uri=t+"/cometd";
        
        address=new InetSocketAddress(host,port);
        
        int nclients=100;
        int size=50;
        int rooms=100;
        int rooms_per_client=1;
        int publish=1000;
        int pause=100;
        int burst=10;
        _targetLatency=1000;
        int maxLatency=5000;

        System.err.print("base[/chat/demo]: ");
        t = in.readLine().trim();
        if (t.length()==0)
            t="/chat/demo";
        String base = t;

        System.err.print("rooms ["+rooms+"]: ");
        t = in.readLine().trim();
        if (t.length()==0)
            t=""+rooms;
        rooms=Integer.parseInt(t);
        
        System.err.print("rooms per client ["+rooms_per_client+"]: ");
        t = in.readLine().trim();
        if (t.length()==0)
            t=""+rooms_per_client;
        rooms_per_client=Integer.parseInt(t);
        
        
        System.err.print("target Latency ["+_targetLatency+"]: ");
        t = in.readLine().trim();
        if (t.length()==0)
            t=""+_targetLatency;
        _targetLatency=Integer.parseInt(t);
        
        System.err.print("max Latency ["+maxLatency+"]: ");
        t = in.readLine().trim();
        if (t.length()==0)
            t=""+maxLatency;
        maxLatency=Integer.parseInt(t);
        
        while(true)
        {
            System.err.println("--");

            System.err.print("clients ["+nclients+"]: ");
            t = in.readLine().trim();
            if (t.length()==0)
                t=""+nclients;
            nclients=Integer.parseInt(t);
            
            if (nclients<rooms || (nclients%rooms)!=0)
            {
                System.err.println("Clients must be a multiple of "+rooms);
                nclients=(nclients/rooms)*rooms;
                continue;
            }
            
            while (clients.size()<nclients)
            {
                int u=clients.size();
                BayeuxClient client = new BayeuxClient(http,address,uri)
                {
                    public void deliver(Client from, Message message)
                    {
                        if (Bayeux.META_SUBSCRIBE.equals(message.get(Bayeux.CHANNEL_FIELD)) &&
                            ((Boolean)message.get(Bayeux.SUCCESSFUL_FIELD)).booleanValue())
                            _subscribed.incrementAndGet();
                        super.deliver(from,message);
                    }
                };
                

                MessageListener listener = new MessageListener()
                {
                    public void deliver(Client fromClient, Client toClient, Message msg)
                    {
                        Object data=(Object)msg.get(AbstractBayeux.DATA_FIELD);
                        if (data!=null)
                        {
                            String msgId=(String)msg.get(AbstractBayeux.ID_FIELD);
                            // System.err.println(name+": "+data);
                            if (msgId!=null)
                            {
                                long latency= System.currentTimeMillis()-Long.parseLong(msgId);
                                synchronized(BayeuxLoadGenerator.this)
                                {
                                    _got++;
                                    if (_maxLatency<latency)
                                        _maxLatency=latency;
                                    if (_minLatency==0 || latency<_minLatency)
                                        _minLatency=latency;
                                    _totalLatency+=latency;
                                    if (latency>_targetLatency)
                                        _over++;
                                }
                            }
                        }
                    }

                };
                client.addListener(listener);
                
                client.start();
                
                clients.add(client);
                if (clients.size()%10==0)
                {
                    int i=clients.size();
                    System.err.println("clients = "+(i>=1000?"":i>=100?"0":i>=10?"00":"000")+i);
                    Thread.sleep(150);
                }
                    
                client.startBatch();
                if (rooms_per_client==1)
                {
                    int room=u%rooms;
                    client.subscribe(room>0?(base+"/"+room):base);
                }
                else
                {
                    for (int i=0;i<rooms_per_client;i++)
                    {
                        int room=_random.nextInt(rooms);
                        client.subscribe(room>0?(base+"/"+room):base);
                    }
                }
                client.endBatch();
            }
            
            while (clients.size()>nclients)
            {
                BayeuxClient client=clients.remove(0);
                client.remove(false);
                _subscribed.addAndGet(-rooms_per_client);
                if (clients.size()%10==0)
                {
                    int i=clients.size();
                    System.err.println("clients = "+(i>=1000?"":i>=100?"0":i>=10?"00":"000")+i);
                    Thread.sleep(300);
                }
            }
            

            Thread.sleep(500);

            while(_subscribed.get()!=nclients*rooms_per_client)
            {
                // System.err.println(destination.toDetailString());
                System.err.println("Subscribed:"+_subscribed.get()+" != "+(nclients*rooms_per_client)+" ...");
                Thread.sleep(1000);
            }
            
            System.err.println("Clients: "+nclients+" subscribed:"+_subscribed.get());

        
            synchronized(this)
            {
                _got=0;
                _minLatency=0;
                _maxLatency=0;
                _totalLatency=0;
                _over=0;
            }


            System.err.print("publish ["+publish+"]: ");
            t = in.readLine().trim();
            if (t.length()==0)
                t=""+publish;
            publish=Integer.parseInt(t);
            
            System.err.print("publish size ["+size+"]: ");
            t = in.readLine().trim();
            if (t.length()==0)
                t=""+size;
            size=Integer.parseInt(t);
            String chat="";
            for (int i=0;i<size;i++)
                chat+="x";

            System.err.print("pause ["+pause+"]: ");
            t = in.readLine().trim();
            if (t.length()==0)
                t=""+pause;
            pause=Integer.parseInt(t);

            System.err.print("batch ["+burst+"]: ");
            t = in.readLine().trim();
            if (t.length()==0)
                t=""+burst;
            burst=Integer.parseInt(t);
           
            long start=System.currentTimeMillis();
            trial:
            for (int i=1;i<=publish;)
            {
                // System.err.print(i);
                // System.err.print(',');
                int u=_random.nextInt(nclients);
                BayeuxClient c = clients.get(u);
                final String name = "Client"+(u>=1000?"":u>=100?"0":u>=10?"00":"000")+u;
                Object msg=new JSON.Literal("{\"user\":\""+name+"\",\"chat\":\""+chat+" "+i+"\"}");
                c.startBatch();
                for (int b=0;b<burst;b++)
                {
                    int room=_random.nextInt(rooms);
                    String id=""+System.currentTimeMillis();
                    c.publish(room>0?(base+"/"+room):base,msg,id);
                    i++;
                    
                    if (i%10==0)
                    {
                        long latency=0;
                        synchronized(this)
                        {
                            if (_got>0) 
                                latency=_totalLatency/_got;
                        }
                        if (latency>maxLatency)
                        {
                            System.err.println("\nABORTED!");
                            break trial;
                        }
                        
                        int l=(int)(latency/100);
                        char dot=l<__DOTS.length()?__DOTS.charAt(l):'~';
                        System.err.print(dot);
                        if (i%1000==0)
                            System.err.println();
                    }
                }
                c.endBatch();

                if (pause>0)
                    Thread.sleep(pause);
                
            }

            Thread.sleep(_maxLatency);

            for (BayeuxClient c : clients)
            {
                if (!c.isPolling())
                    System.err.println("PROBLEM WITH "+c);
            }

            System.err.println();

            long last=0;
            int sleep=100;
            while (_got<(nclients/rooms*rooms_per_client*publish)) 
            {
                System.err.println("Got:"+_got+" < "+(nclients/rooms*rooms_per_client*publish)+" ...");
                Thread.sleep(sleep);
                if (last!=0 && _got==last)
                    break;
                last=_got;
                sleep+=100;
            }
            long end=System.currentTimeMillis();

            System.err.println("Sent "+publish+"x"+size+"b in bursts of "+burst+" to "+nclients+" clients in "+rooms+" rooms. Got "+_got+" of "+(nclients/rooms*rooms_per_client*publish)+" at "+(_got*1000/(end-start))+
                    "/s, latency min/ave/max ="+_minLatency+"/"+(_totalLatency/_got)+"/"+_maxLatency+"ms, "+
                    ((_over*100+49)/_got)+"%>"+_targetLatency+"ms");
        }

    }
    
    
    public static void main(String[] args) throws Exception
    {
        BayeuxLoadGenerator gen = new BayeuxLoadGenerator();
        
        gen.generateLoad();
    }
    
}
