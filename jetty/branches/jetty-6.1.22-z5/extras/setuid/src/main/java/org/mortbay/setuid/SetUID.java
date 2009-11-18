
package org.mortbay.setuid;

public class SetUID
{
    public static final int OK = 0;
    public static final int ERROR = -1;

    public static native int setumask(int mask);
    public static native int setuid(int uid);
    public static native int setgid(int gid);
     
    public static native RLimit getrlimitnofiles();
    public static native int setrlimitnofiles(RLimit rlimit);


    static 
    {
    	System.loadLibrary("setuid");
    }
}
