#ifndef _Included_vpn_jni_interface
#define _Included_vpn_jni_interface

#include <jni.h>
#include <stdlib.h>
#include <memory.h>
#include <android/log.h>

#include "vpn_data.h"
#include "vpn_log.c"
#include "vpn_ip_object.c"
#include "vpn_ip_data_utils.c"
#include "vpn_java_utils.c"

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeInit
        (JNIEnv *env, jobject jobj, jbyteArray jba) {
    IP_Packet *ipPacket = malloc(sizeof(IP_Packet));

    //获取数据报长度
    int len = (*env)->GetArrayLength(env, jba);
    if (len == 0) {
        return;
    }

    char *arrays = (char *) malloc(len * sizeof(char));
    (*env)->GetByteArrayRegion(env, jba, 0, len, (jbyte *) arrays);

    if (arrays == NULL) {
        return;
    }

    //初始化ip数据
    int status = init_ip_packet(ipPacket,arrays);

    if (status == IP_STATUS_SUCCESS){
        if (ipPacket->upper_protocol == (unsigned char)17){

        }
    }

    jclass ip_object_class = (*env)->GetObjectClass(env, jobj);

    jfieldID ip_object_jfieldId = (*env)->GetFieldID(env, ip_object_class, "mPacketRef", "I");

    (*env)->SetIntField(env, jobj, ip_object_jfieldId, (jint) ipPacket);
}

JNIEXPORT jstring JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeGetData
        (JNIEnv *env, jobject jobj, jint jpacket) {
    IP_Packet *nativeIp = (IP_Packet *) jpacket;
    print_ip_packet(nativeIp);
    return charTojstring(env, "test");
}

#endif