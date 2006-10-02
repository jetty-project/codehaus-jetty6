
#include <jni.h>
#include "org_mortbay_setuid_SetUmask.h"
#include <sys/types.h>
#include <unistd.h>
  
JNIEXPORT jint JNICALL 
Java_org_mortbay_setuid_SetUmask_setumask (JNIEnv * jnienv, jclass j, jint umsk)
{
    return(umask(umsk));
}
