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
    Object filter(Object data);
}
