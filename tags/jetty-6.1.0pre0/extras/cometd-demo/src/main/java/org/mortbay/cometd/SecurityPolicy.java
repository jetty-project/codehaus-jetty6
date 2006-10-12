/**
 * 
 */
package org.mortbay.cometd;

import java.util.Map;

/* ------------------------------------------------------------ */
/**
 * @author gregw
 *
 */
public interface SecurityPolicy
{
    boolean canCreate(Client client,Channel channel,Map message);
    boolean canSubscribe(Client client,Channel channel,Map messsage);
    boolean canSend(Client client,Channel channel,Map message);
}
