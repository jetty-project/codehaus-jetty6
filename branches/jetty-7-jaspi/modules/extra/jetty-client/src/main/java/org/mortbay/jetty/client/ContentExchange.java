package org.mortbay.jetty.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.mortbay.io.Buffer;
import org.mortbay.io.BufferUtil;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.util.StringUtil;

/**
 * A CachedExchange that retains all content for later use.
 *
 */
public class ContentExchange extends CachedExchange
{
    int _contentLength = 1024;
    String _encoding = "utf-8";
    ByteArrayOutputStream _responseContent;

    public ContentExchange()
    {
        super(false);
    }

    /* ------------------------------------------------------------ */
    public String getResponseContent() throws UnsupportedEncodingException
    {
        if (_responseContent != null)
        {
            return _responseContent.toString(_encoding);
        }
        return null;
    }

    /* ------------------------------------------------------------ */
    protected void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
        super.onResponseHeader(name,value);
        int header = HttpHeaders.CACHE.getOrdinal(value);
        switch (header)
        {
            case HttpHeaders.CONTENT_LANGUAGE_ORDINAL:
                _contentLength = BufferUtil.toInt(value);
                break;
            case HttpHeaders.CONTENT_TYPE_ORDINAL:

                String mime = StringUtil.asciiToLowerCase(value.toString());
                int i = mime.indexOf("charset=");
                if (i > 0)
                {
                    mime = mime.substring(i + 8);
                    i = mime.indexOf(';');
                    if (i > 0)
                        mime = mime.substring(0,i);
                }
                if (mime != null && mime.length() > 0)
                    _encoding = mime;
                break;
        }
    }

    protected void onResponseContent(Buffer content) throws IOException
    {
        super.onResponseContent( content );
        if (_responseContent == null)
            _responseContent = new ByteArrayOutputStream(_contentLength);
        content.writeTo(_responseContent);
    }
}