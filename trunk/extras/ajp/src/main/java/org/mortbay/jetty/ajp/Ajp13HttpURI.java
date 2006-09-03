package org.mortbay.jetty.ajp;

import java.io.UnsupportedEncodingException;

import org.mortbay.jetty.HttpURI;
import org.mortbay.util.MultiMap;
import org.mortbay.util.URIUtil;
import org.mortbay.util.UrlEncoded;

/**
 * XXX Should not really be a sub class
 *
 * @author Markus Kobler
 */
public class Ajp13HttpURI extends HttpURI {

    private final static String INSECURE_SCHEME = "http";

    private final static String SECURE_SCHEME = "https";

    private String _uri;

    private String _scheme = INSECURE_SCHEME;

    private String _host;

    private int _port;

    private String _pathAndParam;

    private String _query;

    private boolean _sslSecure;


    public String getScheme() {
        return _scheme;
    }

    public void setSslSecure(boolean secure) {
        _scheme = (_sslSecure = secure) ? SECURE_SCHEME : INSECURE_SCHEME;
    }

    public String getHost() {
        return _host;
    }

    public void setHost(String host) {
        _host = host;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(int port) {
        _port = port;
    }

    public void setPathAndParam(String pathAndParam) {
        _pathAndParam = pathAndParam;
    }

    public String getDecodedPath() {
        return _pathAndParam == null ? null : URIUtil.decodePath(_pathAndParam);
    }

    public String getPathAndParam() {
        return _pathAndParam;
    }

    public String getQuery() {
        return _query;
    }

    public void setQuery(String query) {
        _query = query;
    }

    public void decodeQueryTo(MultiMap parameters, String encoding) throws UnsupportedEncodingException {
        if (_query == null) return;
        UrlEncoded.decodeUtf8To(_query.getBytes(), 0, _query.length(), parameters);
    }

    public void clear() {
        _scheme = INSECURE_SCHEME;
        _host = null;
        _port = -1;
        _pathAndParam = null;
        _query = null;
    }

    public String toString() {
        if (_uri == null) {

            StringBuffer uri = new StringBuffer();

            uri.append(_scheme).append("//").append(_host);

            // if standard port does not match scheme then append port 
            if (!(_port == 80 && !_sslSecure || _port == 443 && _sslSecure)) {
                uri.append(':').append(_port);
            }

            uri.append(_pathAndParam);

            if (_query != null) {
                uri.append('?').append(_query);
            }

            _uri = uri.toString();

        }
        return _uri;
    }

    public String getAuthority() {
        throw new UnsupportedOperationException();
    }

    public String getPath() {
        throw new UnsupportedOperationException();
    }

    public String getParam() {
        throw new UnsupportedOperationException();
    }

    public String getFragment() {
        throw new UnsupportedOperationException();
    }

    public void parse(byte[] raw, int offset, int length) {
        throw new UnsupportedOperationException();
    }

    public void parse(String raw) {
        throw new UnsupportedOperationException();
    }

}
