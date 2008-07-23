package org.mortbay.jetty.handler.rewrite;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.LazyList;

/**
 * Groups rules that apply only to a specific virtual host
 * or sets of virtual hosts
 * 
 *  @author Athena Yao
 */

public class VirtualHostRuleContainer extends RuleContainer
{
    private String[] _virtualHosts;

    /* ------------------------------------------------------------ */
    /** Set the virtual hosts that the rules within this container will apply to
     * @param virtualHosts Array of virtual hosts that the rules within this container are applied to. 
     * A null hostname or null/empty array means any hostname is acceptable.
     */
    public void setVirtualHosts( String[] virtualHosts )
    {
        if ( virtualHosts == null )
        {
            _virtualHosts = virtualHosts;
        } 
        else 
        {
            _virtualHosts = new String[virtualHosts.length];
            for ( int i = 0; i < virtualHosts.length; i++ )
                _virtualHosts[i] = normalizeHostname( virtualHosts[i]);
        }
    }

    /* ------------------------------------------------------------ */
    /** Get the virtual hosts that the rules within this container will apply to
     * @param virtualHosts Array of virtual hosts that the rules within this container are applied to. 
     * A null hostname or null/empty array means any hostname is acceptable.
     */
    public String[] getVirtualHosts()
    {
        return _virtualHosts;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param virtualHost add a virtual host to the existing list of virtual hosts
     * A null hostname means any hostname is acceptable 
     */
    public void addVirtualHost(String virtualHost)
    {
        _virtualHosts = (String[])LazyList.addToArray(_virtualHosts,virtualHost,String.class);
    }

    /**
     * Process the contained rules if the request is applicable to the virtual hosts of this rule
     * @param target target field to pass on to the contained rules
     * @param request request object to pass on to the contained rules
     * @param response response object to pass on to the contained rules
     */
    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if(_virtualHosts != null && _virtualHosts.length > 0 )
        {
            String requestHost = normalizeHostname( request.getServerName() );
            for( String ruleHost : _virtualHosts )
            {
                if(ruleHost == null || requestHost.equalsIgnoreCase(ruleHost))
                {
                    return apply(target, request, response);
                }
            }
        }
        else
        {
            return apply(target, request, response);
        }
        return null;
    }

    /* ------------------------------------------------------------ */
    private String normalizeHostname( String host )
    {
        if ( host == null )
            return null;
        
        if ( host.endsWith( "." ) )
            return host.substring( 0, host.length() -1);
      
            return host;
    }

}
