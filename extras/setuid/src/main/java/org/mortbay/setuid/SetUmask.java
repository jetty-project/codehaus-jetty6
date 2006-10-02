/**
 * 
 */
package org.mortbay.setuid;

/**
 * @author janb
 *
 */
public class SetUmask
{
    public static final int OK = 0;
    public static final int ERROR = -1;

    public static native int setumask(int umask);

    static 
    {
        System.loadLibrary("setumask");
    }
}
