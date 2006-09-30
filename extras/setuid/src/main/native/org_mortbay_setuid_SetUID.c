
#include <jni.h>
#include "org_mortbay_setuid_SetUID.h"
#include <sys/types.h>
#include <unistd.h>
  
JNIEXPORT jint JNICALL 
Java_org_mortbay_setuid_SetUID_setuid (JNIEnv * jnienv, jclass j, jint uid)
{
    return((jint)setuid((uid_t)uid));
}
