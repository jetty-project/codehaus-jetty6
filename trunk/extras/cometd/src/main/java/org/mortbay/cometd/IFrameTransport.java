package org.mortbay.cometd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public class IFrameTransport extends AbstractTransport
{
    PrintWriter _writer;

    public void preample(HttpServletResponse response) throws IOException
    {
        preample(response,null);
    }

    public boolean preample(HttpServletResponse response, Map reply) throws IOException
    {
        String channel=(String)reply.get("channel");
        if (!"/meta/connect".equals(channel)&&!"/meta/reconnect".equals(channel))
            reply=null;

        response.setContentType("text/html; charset=UTF-8");
        _writer=response.getWriter();

        _writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        _writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        _writer.println("<head>");
        _writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></meta>");
        _writer.println("<title>cometd: over jetty</title>");

        if (reply!=null)
        {
            _writer.write("<script type=\"text/javascript\">");
            _writer.write("window.parent.cometd.deliver([");
            _writer.write(JSON.toString(reply));
            _writer.write("]);");
            _writer.write("</script>");
        }
        _writer.println("</head>");
        _writer.println("<body onload=\"window.parent.cometd.tunnelCollapse();\">");
        _writer.flush();

        return reply!=null;
    }

    public void encode(Map reply) throws IOException
    {
        _writer.write("<br /><script type=\"text/javascript\">");
        _writer.write("window.parent.cometd.deliver([");
        _writer.write(JSON.toString(reply));
        _writer.write("]);");
        _writer.write("</script><br/>");
        for (int i=0; i<16; i++)
            _writer.write("                                                                                                                                ");
        _writer.write("<br/>");
        _writer.flush();
    }

    public void encode(List replies) throws IOException
    {
        if (replies==null)
            return;

        try
        {
            _writer.write("<br /><script type=\"text/javascript\">");
            _writer.write("window.parent.cometd.deliver([");

            for (int i=0; i<replies.size(); i++)
            {
                // encode((Map)replies.get(i));
                // do multiple messages in one deliver
                _writer.write(JSON.toString(replies.get(i)));
                if (i!=replies.size()-1)
                    _writer.write(", ");
            }
            _writer.write("]);");
            _writer.write("</script><br/>");
            for (int i=0; i<16; i++)
                _writer
                        .write("                                                                                                                                ");
            _writer.write("<br/>");
            _writer.flush();
        }
        catch (Exception e)
        {
            System.err.println("exception encoding, messages lost?");
            for (int i=0; i<replies.size(); i++)
            {
                System.err.println(JSON.toString(replies.get(i)));
            }
            e.printStackTrace();
        }
    }

    public void complete() throws IOException
    {
        // _writer.write("<script
        // type=\"text/javascript\">window.parent.cometd.tunnelCollapse();");
        // _writer.write("</script>");
        _writer.write("</body>");
        _writer.write("</html>");
        _writer.flush();
    }

    public boolean keepAlive() throws IOException
    {
        return false;
    }

    public void initTunnel(HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html; charset=utf-8");
        _writer=response.getWriter();
        _writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        _writer.println(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        _writer.println("");
        _writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        _writer.println(" <head>");
        _writer.println("  <title>cometd: The Long Tail of Comet</title>");
        _writer.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></meta>");
        _writer.println("  <script type=\"text/javascript\">");
        _writer.println("   // window.parent.dojo.debug(\"tunnelInit\");");
        _writer.println("   var noInit = false;");
        _writer.println("   var domain = \"\";");
        _writer.println("   function init(){");
        _writer.println("    var sparams = document.location.search;");
        _writer.println("    if(sparams.length >= 0){");
        _writer.println("     if(sparams.charAt(0) == \"?\"){");
        _writer.println("      sparams = sparams.substring(1);");
        _writer.println("     }");
        _writer.println("     var ss = (sparams.indexOf(\"&amp;\") >= 0) ? \"&amp;\" : \"&\";");
        _writer.println("     sparams = sparams.split(ss);");
        _writer.println("     for(var x=0; x<sparams.length; x++){");
        _writer.println("      var tp = sparams[x].split(\"=\");");
        _writer.println("      if(typeof window[tp[0]] != \"undefined\"){");
        _writer.println("       window[tp[0]] = ((tp[1]==\"true\")||(tp[1]==\"false\")) ? eval(tp[1]) : tp[1];");
        _writer.println("      }");
        _writer.println("     }");
        _writer.println("    }");
        _writer.println("    if(noInit){ return; }");
        _writer.println("    /*");
        _writer.println("    if(domain.length > 0){");
        _writer.println("     document.domain = domain;");
        _writer.println("    }");
        _writer.println("    */");
        _writer.println("    if(window.parent != window){");
        _writer.println("     //Notify parent that we are loaded.");
        _writer.println("     window.parent.cometd.tunnelInit(window.location, document.domain);");
        _writer.println("    }");
        _writer.println("   }");
        _writer.println("  </script>");
        _writer.println(" </head>");
        _writer.println(" <body onload=\"try{ init(); }catch(e){ alert(e); }\">");
        _writer.println("  <h4>cometd: The Long Tail of Comet</h4>");
        _writer.println(" </body>");
        _writer.println("</html>");

    }

}
