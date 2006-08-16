/**
 * 
 */
package org.mortbay.cometd;

/* ------------------------------------------------------------ */
/**
 * @author gregw
 *
 */
public interface DataFilter
{
    Object filter(Object data);
}
