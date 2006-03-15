package com.acme;

import java.text.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;
import java.util.*;

public class Date2Tag extends SimpleTagSupport
{
    String format;
    
    public void setFormat(String value) {
        this.format = value;
    }

    public void doTag() throws JspException, IOException {
        String formatted = 
            new SimpleDateFormat("long".equals(format)?"EEE 'the' d:MMM:yyyy":"d:MM:yy")
            .format(new Date());
        StringTokenizer tok = new StringTokenizer(formatted,":");
        JspContext context = getJspContext();
        context.setAttribute("day", tok.nextToken() );
        context.setAttribute("month", tok.nextToken() );
        context.setAttribute("year", tok.nextToken() );

        JspFragment fragment = getJspBody();
        fragment.invoke(null);
    }
}

