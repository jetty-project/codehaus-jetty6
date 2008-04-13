package org.mortbay.jetty.handler.rewrite;

import org.mortbay.jetty.servlet.PathMap;
import org.mortbay.util.URIUtil;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Rule implementing the legacy API of RewriteHandler
 * @author gregw
 *
 */
public class LegacyRule extends Rule
{
    private PathMap _rewrite = new PathMap(true);
    
    public LegacyRule()
    {
        _handling = false;
        _terminating = false;
    }

    /* ------------------------------------------------------------ */
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        Map.Entry<?,?> rewrite =_rewrite.getMatch(target);
        
        if (rewrite!=null && rewrite.getValue()!=null)
        {
            target=URIUtil.addPaths(rewrite.getValue().toString(),
                    PathMap.pathInfo(rewrite.getKey().toString(),target));

            return target;
        }
        
        return null;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return A {@link PathMap} of the rewriting rules.
     */
    public PathMap getRewrite()
    {
        return _rewrite;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param rewrite A {@link PathMap} of the rewriting rules. Only 
     * prefix paths should be included.
     */
    public void setRewrite(PathMap rewrite)
    {
        _rewrite=rewrite;
    }
    
    
    /* ------------------------------------------------------------ */
    /** Add a path rewriting rule
     * @param pattern The path pattern to match. The pattern must start with / and may use
     * a trailing /* as a wildcard.
     * @param prefix The path prefix which will replace the matching part of the path.
     */
    public void addRewriteRule(String pattern, String prefix)
    {
        if (pattern==null || pattern.length()==0 || !pattern.startsWith("/"))
            throw new IllegalArgumentException();
        if (_rewrite==null)
            _rewrite=new PathMap(true);
        _rewrite.put(pattern,prefix);
    }


}
