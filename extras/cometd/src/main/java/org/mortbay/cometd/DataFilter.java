/**
 * 
 */
package org.mortbay.cometd;

/* ------------------------------------------------------------ */
/** Data Filter
 * Data filters are used to transform data as it is sent to a Channel.
 * 
 * @author gregw
 *
 */
public interface DataFilter
{
    void init (Object init);
    
    /**
     * @param data
     * @param from
     * @return The filtered data.
     * @throws IllegalStateException If the message should be aborted
     */
    Object filter(Object data, Client from) throws IllegalStateException;
}
