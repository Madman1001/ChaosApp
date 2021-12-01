#ifndef _Included_vpn_jni_interface
#define _Included_vpn_jni_interface

#include <jni.h>
#include <stdlib.h>
#include <memory.h>
#include <android/log.h>

#include "vpn_data.c"
#include "vpn_log.c"
#include "vpn_object_ip.c"
#include "vpn_object_udp.c"
#include "vpn_object_tcp.c"
#include "vpn_java_utils.c"

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeInit
        (JNIEnv *env, jobject jobj, jbyteArray jba) {
    IP_Packet *ip_packet = malloc(sizeof(IP_Packet));

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
    int status = init_ip_packet(ip_packet, arrays);

    if (status == IP_STATUS_SUCCESS && ip_packet->data != NULL){
        // 解析运输层协议
        switch (ip_packet->upper_protocol) {
            case PACKET_TYPE_UDP : {
                UDP_Packet *udp_packet = malloc(sizeof(UDP_Packet));
                init_udp_packet(udp_packet,ip_packet->data, ip_packet->total_length - ip_packet->head_length * 4);
            }
            case PACKET_TYPE_TCP : {
                TCP_Packet *tcp_packet = malloc(sizeof(TCP_Packet));
                init_tcp_packet(tcp_packet,ip_packet->data, ip_packet->total_length - ip_packet->head_length * 4);
            }
        }
    }

    TAG_E("init over");

    jclass ip_object_class = (*env)->GetObjectClass(env, jobj);

    jfieldID ip_object_jfieldId = (*env)->GetFieldID(env, ip_object_class, "mPacketRef", "I");

    (*env)->SetIntField(env, jobj, ip_object_jfieldId, (jint) ip_packet);
}

JNIEXPORT jstring JNICALL Java_com_lhr_vpn_util_ByteLog_nativeGetByteBufferString
        (JNIEnv *env, jobject jobj, jbyteArray jba) {
    int len = (*env)->GetArrayLength(env,jba);
    jbyte *arrays = (jbyte *) malloc(len * sizeof(jbyte));
    unsigned char* chars = malloc(len * 8 * sizeof(unsigned char) + len + 1);
    (*env)->GetByteArrayRegion(env, jba, 0, len, arrays);
    int byteIndex = 0;
    int charsIndex = 0;
    for (; byteIndex < len; ++byteIndex) {
        unsigned char uc = (unsigned char)arrays[byteIndex];
        unsigned char sign = 0x80;
        int z = 0;
        for (; z < 8; z++){
            if (uc & sign){
                chars[charsIndex++] = '1';
            }
            else{
                chars[charsIndex++] = '0';
            }
            sign = sign >> 1;
        }
        chars[charsIndex++] = ',';
    }
    chars[charsIndex - 1] = '\0';

    jstring message = charTojstring(env,(const char*)chars);

    //检查是否有异常
    jboolean has_exception =  (*env)->ExceptionCheck(env);
    if (has_exception) {
        (*env)->ExceptionDescribe(env);
        //清空异常
        (*env)->ExceptionClear(env);
        (*env)->ThrowNew(env,(*env)->FindClass(env,"java/lang/Exception"),"创建字符串出现异常");
        return NULL;
    }

    free(chars);

    free(arrays);

    return message;
}

#endif