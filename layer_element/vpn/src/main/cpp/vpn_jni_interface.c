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

enum DATA_TYPE {
    IP_VERSION = 1,
    IP_UPPER_PROTOCOL = 2,
    IP_DATA = 3,
    IP_SOURCE_ADDRESS = 4,
    IP_TARGET_ADDRESS = 5,
    UDP_SOURCE_PORT = 6,
    UDP_TARGET_PORT = 7,
    UDP_DATA = 8,
    TCP_SOURCE_PORT = 9,
    TCP_TARGET_PORT = 10
};

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

    if (status == IP_STATUS_SUCCESS && ip_packet->data != NULL) {
        // 解析运输层协议
        // 此处存在内存泄露问题 ip_packet->data
        switch (ip_packet->upper_protocol) {
            case PACKET_TYPE_UDP : {
                UDP_Packet *udp_packet = malloc(sizeof(UDP_Packet));
                init_udp_packet(udp_packet, ip_packet->data,
                                ip_packet->total_length - ip_packet->head_length * 4);
                free(ip_packet->data);
                ip_packet->data = udp_packet;
                print_udp_packet(ip_packet->data);
                break;
            }
            case PACKET_TYPE_TCP : {
                TCP_Packet *tcp_packet = malloc(sizeof(TCP_Packet));
                init_tcp_packet(tcp_packet, ip_packet->data,
                                ip_packet->total_length - ip_packet->head_length * 4);
                free(ip_packet->data);
                ip_packet->data = tcp_packet;
                print_tcp_packet(ip_packet->data);
                break;
            }
        }
    }
    print_ip_packet(ip_packet);

    TAG_E("init over");

    jclass ip_object_class = (*env)->GetObjectClass(env, jobj);

    jfieldID ip_object_jfieldId = (*env)->GetFieldID(env, ip_object_class, "mPacketRef", "J");

    TAG_E("void* %d %d", sizeof(&ip_packet),sizeof(*ip_packet));

    (*env)->SetLongField(env, jobj, ip_object_jfieldId, (jlong) ip_packet);
}

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeRelease
        (JNIEnv *env, jobject jobj, jlong dataRef) {
    if (dataRef == 0) {
        return;
    }

    IP_Packet *ipPacket = (IP_Packet *) dataRef;
//    if (ipPacket->data != NULL) {
//        if (ipPacket->upper_protocol == PACKET_TYPE_TCP) {
//            release_tcp_packet((TCP_Packet *) ipPacket->data);
//        } else if (ipPacket->upper_protocol == PACKET_TYPE_UDP) {
//            release_udp_packet((UDP_Packet *) ipPacket->data);
//        }
//        ipPacket->data = NULL;
//    }
//    release_ip_packet(ipPacket);
    free(ipPacket);
}

JNIEXPORT jobject JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeGetData
        (JNIEnv *env, jobject jobj, jlong dataRef, jint dataType) {
    if (dataRef == 0) {
        //数据处理异常
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "数据为空");
        return NULL;
    }

    IP_Packet *ipPacket = (IP_Packet *) dataRef;

    jobject result = NULL;

    switch (dataType) {
        case IP_VERSION: {
            result = intToInteger(env, (int) ipPacket->version);
            break;
        }
        case IP_UPPER_PROTOCOL: {
            result = intToInteger(env, (int) ipPacket->upper_protocol);
            break;
        }
        case IP_DATA: {
            if (ipPacket->data != NULL) {
                int dataLength = (int) ipPacket->total_length - (int) ipPacket->head_length * 4;
                jbyteArray bytes = (*env)->NewByteArray(env, dataLength);
                (*env)->SetByteArrayRegion(env, bytes, 0, dataLength, (jbyte *) ipPacket->data);
                result = bytes;
            }
            break;
        }
        case IP_SOURCE_ADDRESS: {
            result = intToInteger(env, (int) ipPacket->source_ip_address);
            break;
        }
        case IP_TARGET_ADDRESS: {
            result = intToInteger(env, (int) ipPacket->target_ip_address);
            break;
        }
        case UDP_SOURCE_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL){
                UDP_Packet* udpPacket = (UDP_Packet*)ipPacket->data;
                result = intToInteger(env, (int) udpPacket->source_port);
            }
            break;
        }
        case UDP_TARGET_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL){
                UDP_Packet* udpPacket = (UDP_Packet*)ipPacket->data;
                result = intToInteger(env, (int) udpPacket->target_port);
            }
            break;
        }
        case UDP_DATA: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL) {
                UDP_Packet* udpPacket = (UDP_Packet*)ipPacket->data;
                if (udpPacket->data != NULL){
                    int dataLength = (unsigned short)udpPacket->udp_data_length;
                    jbyteArray bytes = (*env)->NewByteArray(env, dataLength);
                    (*env)->SetByteArrayRegion(env, bytes, 0, dataLength, (jbyte *) udpPacket->data);
                    result = bytes;
                }
            }
            break;
        }
    }

    return result;
}

JNIEXPORT jstring JNICALL Java_com_lhr_vpn_util_ByteLog_nativeGetByteBufferString
        (JNIEnv *env, jobject jobj, jbyteArray jba) {
    int len = (*env)->GetArrayLength(env, jba);
    jbyte *arrays = (jbyte *) malloc(len * sizeof(jbyte));
    unsigned char *chars = malloc(len * 8 * sizeof(unsigned char) + len + 1);
    (*env)->GetByteArrayRegion(env, jba, 0, len, arrays);
    int byteIndex = 0;
    int charsIndex = 0;
    for (; byteIndex < len; ++byteIndex) {
        unsigned char uc = (unsigned char) arrays[byteIndex];
        unsigned char sign = 0x80;
        int z = 0;
        for (; z < 8; z++) {
            if (uc & sign) {
                chars[charsIndex++] = '1';
            } else {
                chars[charsIndex++] = '0';
            }
            sign = sign >> 1;
        }
        chars[charsIndex++] = ',';
    }
    chars[charsIndex - 1] = '\0';

    jstring message = charTojstring(env, (const char *) chars);

    //检查是否有异常
    jboolean has_exception = (*env)->ExceptionCheck(env);
    if (has_exception) {
        (*env)->ExceptionDescribe(env);
        //清空异常
        (*env)->ExceptionClear(env);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "创建字符串出现异常");
        return NULL;
    }

    free(chars);

    free(arrays);

    return message;
}

#endif