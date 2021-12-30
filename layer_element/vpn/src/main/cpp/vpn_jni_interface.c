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
    IP_FLAG = 6,
    IP_OFFSET_FRAG = 7,
    IP_TIME_TO_LIVE = 8,
    IP_IDENTIFICATION = 9,

    UDP_SOURCE_PORT = 10,
    UDP_TARGET_PORT = 11,
    UDP_DATA = 12,

    TCP_SOURCE_PORT = 13,
    TCP_TARGET_PORT = 14,
    TCP_SERIAL_NUMBER = 15,
    TCP_VERIFY_SERIAL_NUMBER = 16,
    TCP_CONTROL_SIGN = 17,
    TCP_WINDOW_SIZE = 18,
    TCP_URGENT_POINTER = 19,
    TCP_OPTIONS = 20,
    TCP_DATA = 21
};

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeInit
        (JNIEnv *env, jobject jobj) {
    IP_Packet *ip_packet = generate_ip_packet();

    jclass ip_object_class = (*env)->GetObjectClass(env, jobj);

    jfieldID ip_object_jfieldId = (*env)->GetFieldID(env, ip_object_class, "mPacketRef", "J");

    (*env)->SetLongField(env, jobj, ip_object_jfieldId, (jlong) ip_packet);
    TAG_E("void* %d %d", sizeof(&ip_packet), sizeof(*ip_packet));
}

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeSetRawData
        (JNIEnv *env, jobject jobj, jlong dataRef, jbyteArray jba) {
    if (dataRef == 0) {
        //数据处理异常
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "数据为空");
        return;
    }

    IP_Packet *ip_packet = (IP_Packet *) dataRef;

    //获取数据报长度
    int len = (*env)->GetArrayLength(env, jba);
    if (len == 0) {
        return;
    }

    unsigned char *arrays = (unsigned char *) malloc(len * sizeof(unsigned char));
    (*env)->GetByteArrayRegion(env, jba, 0, len, (jbyte *) arrays);

    if (arrays == NULL) {
        return;
    }

    ip_packet->version = ip_read_version(arrays);
    if (ip_packet->version != IP_VERSION_V4) {
        TAG_E("Unable to parse data, ip version is %d", ip_packet->version);
        return;
    }

    //初始化ip数据
    int status = init_ip_packet(ip_packet, arrays);

    if (status == IP_STATUS_SUCCESS && ip_packet->data != NULL) {
        // 解析运输层协议
        switch (ip_packet->upper_protocol) {
            case PACKET_TYPE_UDP : {
                UDP_Packet *udp_packet = generate_udp_packet();
                init_udp_packet(udp_packet, ip_packet->data,
                                ip_packet->total_length - ip_packet->head_length * 4);
                free(ip_packet->data);
                ip_packet->data = udp_packet;
                print_udp_packet(ip_packet->data);
                break;
            }
            case PACKET_TYPE_TCP : {
                TCP_Packet *tcp_packet = generate_tcp_packet();
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
}

JNIEXPORT jbyteArray JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeGetRawData
        (JNIEnv *env, jobject jobj, jlong dataRef) {
    if (dataRef == 0) {
        //数据处理异常
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "数据为空");
        return NULL;
    }

    IP_Packet *ip_packet = (IP_Packet *) dataRef;

    if (ip_packet->total_length <= 0) {
        //数据处理异常
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "ip 数据解析异常");
        return NULL;
    }

    //获取数据报长度
    jbyteArray rawData = (*env)->NewByteArray(env, ip_packet->total_length);

    //检查是否有异常
    jboolean has_exception = (*env)->ExceptionCheck(env);
    if (has_exception) {
        (*env)->ExceptionDescribe(env);
        //清空异常
        (*env)->ExceptionClear(env);
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "创建Byte数组出现异常");
        return NULL;
    }

    void *temp_data = ip_packet->data;

    switch (ip_packet->upper_protocol) {
        case PACKET_TYPE_UDP: {
            UDP_Packet *udpPacket = (UDP_Packet *) temp_data;
            udpPacket->check_sum = 0x0000;
            ip_packet->data = udp_packet_to_binary(udpPacket);
            udpPacket->check_sum = get_udp_check_sum(ip_packet->source_ip_address,
                                                     ip_packet->target_ip_address,
                                                     udpPacket->total_length,
                                                     ip_packet->data);
            udp_write_check_sum(ip_packet->data, udpPacket->check_sum);
            break;
        }
        case PACKET_TYPE_TCP: {
            TCP_Packet *tcpPacket = (TCP_Packet *) temp_data;
            TAG_D("tcp >> old check sum 0x%04x", tcpPacket->check_sum);
            tcpPacket->check_sum = 0x0000;
            ip_packet->data = tcp_packet_to_binary(tcpPacket);
            tcpPacket->check_sum = get_tcp_check_sum(ip_packet->source_ip_address,
                                                     ip_packet->target_ip_address,
                                                     tcpPacket->total_length,
                                                     ip_packet->data);
            tcp_write_check_sum(ip_packet->data, tcpPacket->check_sum);
            TAG_D("tcp >> new check sum 0x%04x", tcpPacket->check_sum);
            break;
        }
    }

    ip_packet->head_check_sum = 0x0000;
    unsigned char *ip_byte_data = ip_packet_to_binary(ip_packet);
    ip_packet->head_check_sum = get_ip_check_sum(ip_packet->head_length * 4, ip_byte_data);
    ip_write_head_check_sum(ip_byte_data, ip_packet->head_check_sum);

    ip_packet->data = temp_data;

    (*env)->SetByteArrayRegion(env, rawData, 0, ip_packet->total_length, (jbyte *) ip_byte_data);

    free(ip_byte_data);

    return rawData;
}

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeRelease
        (JNIEnv *env, jobject jobj, jlong dataRef) {
    if (dataRef == 0) {
        return;
    }

    IP_Packet *ipPacket = (IP_Packet *) dataRef;
    if (ipPacket->data != NULL) {
        switch (ipPacket->upper_protocol) {
            case PACKET_TYPE_TCP: {
                release_tcp_packet((TCP_Packet *) ipPacket->data);
                break;
            }
            case PACKET_TYPE_UDP: {
                release_udp_packet((UDP_Packet *) ipPacket->data);
                break;
            }
        }
        ipPacket->data = NULL;
    }
    release_ip_packet(ipPacket);
    free(ipPacket);
}

JNIEXPORT jobject JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeGetAttribute
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
            result = int2Integer(env, (int) ipPacket->version);
            break;
        }
        case IP_UPPER_PROTOCOL: {
            result = int2Integer(env, (int) ipPacket->upper_protocol);
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
            result = int2Integer(env, (int) ipPacket->source_ip_address);
            break;
        }
        case IP_TARGET_ADDRESS: {
            result = int2Integer(env, (int) ipPacket->target_ip_address);
            break;
        }
        case IP_FLAG: {
            result = char2Byte(env, ipPacket->flag);
            break;
        }
        case IP_IDENTIFICATION: {
            result = short2Short(env, ipPacket->identification);
            break;
        }
        case IP_TIME_TO_LIVE: {
            result = int2Integer(env, (int) ipPacket->time_to_live);
            break;
        }
        case IP_OFFSET_FRAG: {
            result = int2Integer(env, (int) ipPacket->offset_frag);
            break;
        }
        case UDP_SOURCE_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL) {
                UDP_Packet *udpPacket = (UDP_Packet *) ipPacket->data;
                result = int2Integer(env, (int) udpPacket->source_port);
            }
            break;
        }
        case UDP_TARGET_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL) {
                UDP_Packet *udpPacket = (UDP_Packet *) ipPacket->data;
                result = int2Integer(env, (int) udpPacket->target_port);
            }
            break;
        }
        case UDP_DATA: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL) {
                UDP_Packet *udpPacket = (UDP_Packet *) ipPacket->data;
                if (udpPacket->data != NULL) {
                    int dataLength = (unsigned short) udpPacket->data_length;
                    jbyteArray bytes = (*env)->NewByteArray(env, dataLength);
                    (*env)->SetByteArrayRegion(env, bytes, 0, dataLength,
                                               (jbyte *) udpPacket->data);
                    result = bytes;
                }
            }
            break;
        }
        case TCP_CONTROL_SIGN: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                result = char2Byte(env, tcpPacket->control_sign);
            }
            break;
        }
        case TCP_SOURCE_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                result = int2Integer(env, (int) tcpPacket->source_port);
            }
            break;
        }
        case TCP_TARGET_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                result = int2Integer(env, (int) tcpPacket->target_port);
            }
            break;
        }
        case TCP_SERIAL_NUMBER: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                result = long2Long(env, (unsigned long) tcpPacket->serial_number);
            }
            break;
        }
        case TCP_VERIFY_SERIAL_NUMBER: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                result = long2Long(env, (unsigned long) tcpPacket->verify_serial_number);
            }
            break;
        }
        case TCP_WINDOW_SIZE: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                result = int2Integer(env, (int) tcpPacket->window_size);
            }
            break;
        }
        case TCP_URGENT_POINTER: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                result = int2Integer(env, (int) tcpPacket->urgent_pointer);
            }
            break;
        }
        case TCP_DATA: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                if (tcpPacket->data != NULL) {
                    int dataLength = (unsigned short) tcpPacket->data_length;
                    jbyteArray bytes = (*env)->NewByteArray(env, dataLength);
                    (*env)->SetByteArrayRegion(env, bytes, 0, dataLength,
                                               (jbyte *) tcpPacket->data);
                    result = bytes;
                }
            }
            break;
        }
        case TCP_OPTIONS: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                if (tcpPacket->options != NULL) {
                    int dataLength = 0;
                    for (int i = 0; i < tcpPacket->options_length; ++i) {
                        dataLength += tcpPacket->options[i].length;
                    }
                    if (dataLength > 0) {
                        unsigned char *optionData = malloc(dataLength);
                        int index = 0;
                        for (int i = 0; i < tcpPacket->options_length; ++i) {
                            TCP_Option *tcpOption = &tcpPacket->options[i];
                            optionData[index++] = tcpOption->kind;
                            if (tcpOption->kind == (unsigned char) 0x00 ||
                                tcpOption->kind == (unsigned char) 0x01) {
                                continue;
                            }
                            optionData[index++] = tcpOption->length;
                            if (tcpOption->length - 2 <= 0) {
                                continue;
                            }
                            int len = tcpOption->length - 2;
                            for (int j = 0; j < len; ++j) {
                                optionData[index++] = tcpOption->data[j];
                            }
                        }
                        jbyteArray bytes = (*env)->NewByteArray(env, dataLength);
                        (*env)->SetByteArrayRegion(env, bytes, 0, dataLength,
                                                   (jbyte *) optionData);
                        result = bytes;
                        free(optionData);
                    }
                }
            }
            break;
        }
    }
    return result;
}

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeSetAttribute
        (JNIEnv *env, jobject jobj, jlong dataRef, jint dataType, jobject data) {
    if (dataRef == 0) {
        //数据处理异常
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "数据为空");
    }

    IP_Packet *ipPacket = (IP_Packet *) dataRef;
    switch (dataType) {
        case IP_UPPER_PROTOCOL: {
            ipPacket->upper_protocol = (unsigned char) Integer2int(env, data);
            if (ipPacket->data != NULL) {
                free(ipPacket->data);
            }
            switch (ipPacket->upper_protocol) {
                case PACKET_TYPE_UDP: {
                    UDP_Packet *udpPacket = generate_udp_packet();
                    ipPacket->data = udpPacket;
                    ipPacket->total_length = ipPacket->head_length * 4 + udpPacket->total_length;
                    break;
                }
                case PACKET_TYPE_TCP: {
                    TCP_Packet *tcpPacket = generate_tcp_packet();
                    ipPacket->data = tcpPacket;
                    ipPacket->total_length = ipPacket->head_length * 4 + tcpPacket->total_length;
                    break;
                }
            }
            break;
        }
        case IP_SOURCE_ADDRESS: {
            ipPacket->source_ip_address = (unsigned int) Integer2int(env, data);
            break;
        }
        case IP_TARGET_ADDRESS: {
            ipPacket->target_ip_address = (unsigned int) Integer2int(env, data);
            break;
        }
        case IP_FLAG: {
            ipPacket->flag = Byte2char(env, data);
            break;
        }
        case IP_IDENTIFICATION: {
            ipPacket->identification = Short2short(env, data);
            break;
        }
        case IP_TIME_TO_LIVE: {
            ipPacket->time_to_live = (unsigned char) Integer2int(env, data);
            break;
        }
        case IP_OFFSET_FRAG: {
            ipPacket->offset_frag = (unsigned short) Integer2int(env, data);
            break;
        }
        case UDP_TARGET_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL) {
                UDP_Packet *udpPacket = (UDP_Packet *) ipPacket->data;
                udpPacket->target_port = (unsigned short) Integer2int(env, data);
            }
            break;
        }
        case UDP_SOURCE_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL) {
                UDP_Packet *udpPacket = (UDP_Packet *) ipPacket->data;
                udpPacket->source_port = (unsigned short) Integer2int(env, data);
            }
            break;
        }
        case UDP_DATA: {
            if (ipPacket->upper_protocol == PACKET_TYPE_UDP && ipPacket->data != NULL) {
                UDP_Packet *udpPacket = (UDP_Packet *) ipPacket->data;
                jclass dataClass = (*env)->FindClass(env, "[B");
                if ((*env)->IsInstanceOf(env, data, dataClass) == JNI_TRUE) {
                    jbyteArray byteData = (jbyteArray) data;
                    jint dataLength = (*env)->GetArrayLength(env, byteData);
                    int diff = dataLength - udpPacket->data_length;
                    udpPacket->data_length = dataLength;
                    udpPacket->total_length += diff;
                    if (udpPacket->data == NULL) {
                        udpPacket->data = malloc(udpPacket->data_length);
                    } else {
                        udpPacket->data = realloc(udpPacket->data, udpPacket->data_length);
                    }
                    ipPacket->total_length += diff;
                    (*env)->GetByteArrayRegion(env, byteData, 0, dataLength,
                                               (jbyte *) udpPacket->data);
                }
            }
            break;
        }
        case TCP_CONTROL_SIGN: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                tcpPacket->control_sign = (unsigned char) Byte2char(env, data);
            }
            break;
        }
        case TCP_SOURCE_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                tcpPacket->source_port = (unsigned short) Integer2int(env, data);
            }
            break;
        }
        case TCP_TARGET_PORT: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                tcpPacket->target_port = (unsigned short) Integer2int(env, data);
            }
            break;
        }
        case TCP_SERIAL_NUMBER: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                tcpPacket->serial_number = (unsigned int) Long2long(env, data);
            }
            break;
        }
        case TCP_VERIFY_SERIAL_NUMBER: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                tcpPacket->verify_serial_number = (unsigned int) Long2long(env, data);
            }
            break;
        }
        case TCP_WINDOW_SIZE: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                tcpPacket->window_size = (unsigned short) Integer2int(env, data);
            }
            break;
        }
        case TCP_URGENT_POINTER: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                tcpPacket->urgent_pointer = (unsigned short) Integer2int(env, data);
            }
            break;
        }
        case TCP_DATA: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                jclass dataClass = (*env)->FindClass(env, "[B");
                if ((*env)->IsInstanceOf(env, data, dataClass) == JNI_TRUE) {
                    jbyteArray byteData = (jbyteArray) data;
                    jint dataLength = (*env)->GetArrayLength(env, byteData);
                    int diff = dataLength - tcpPacket->data_length;
                    tcpPacket->data_length = dataLength;
                    tcpPacket->total_length = tcpPacket->head_length * 4 + tcpPacket->data_length;
                    if (tcpPacket->data == NULL) {
                        tcpPacket->data = malloc(tcpPacket->data_length);
                    } else {
                        tcpPacket->data = realloc(tcpPacket->data, tcpPacket->data_length);
                    }
                    ipPacket->total_length += diff;
                    (*env)->GetByteArrayRegion(env, byteData, 0, dataLength,
                                               (jbyte *) tcpPacket->data);
                }
            }
            break;
        }
        case TCP_OPTIONS: {
            if (ipPacket->upper_protocol == PACKET_TYPE_TCP && ipPacket->data != NULL) {
                TCP_Packet *tcpPacket = (TCP_Packet *) ipPacket->data;
                jclass dataClass = (*env)->FindClass(env, "[B");
                if ((*env)->IsInstanceOf(env, data, dataClass) == JNI_TRUE) {
                    jbyteArray byteData = (jbyteArray) data;
                    jint dataLength = (*env)->GetArrayLength(env, byteData);
                    release_tcp_option(tcpPacket);
                    if ((20 + dataLength) % 4 != 0) {
                        tcpPacket->head_length = (20 + dataLength) / 4 + 1;
                    } else {
                        tcpPacket->head_length = (20 + dataLength) / 4;
                    }
                    tcpPacket->total_length = tcpPacket->head_length * 4 + tcpPacket->data_length;
                    ipPacket->total_length = ipPacket->head_length * 4 + tcpPacket->total_length;
                    unsigned char *tempData = malloc(dataLength);
                    (*env)->GetByteArrayRegion(env, byteData, 0, dataLength, (jbyte *) tempData);
                    tcpPacket->options = tcp_read_options(tempData, 0, dataLength,
                                                          &(tcpPacket->options_length));
                    free(tempData);
                }
            }
            break;
        }
    }
}

JNIEXPORT jstring JNICALL Java_com_lhr_vpn_util_ByteLog_nativeBinaryToString
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

JNIEXPORT jstring JNICALL Java_com_lhr_vpn_util_ByteLog_nativeHexToString
        (JNIEnv *env, jobject jobj, jbyteArray jba) {
    int len = (*env)->GetArrayLength(env, jba);
    jbyte *arrays = (jbyte *) malloc(len * sizeof(jbyte));
    unsigned char *chars = malloc(len * 2 * sizeof(unsigned char) + len + 1);
    (*env)->GetByteArrayRegion(env, jba, 0, len, arrays);
    int byteIndex = 0;
    int charsIndex = 0;

    char hexTable[] = "0123456789abcdef";
    for (int i = 0; i < len; i++) {
        unsigned char uc = (unsigned char) arrays[byteIndex++];
        chars[charsIndex++] = hexTable[0x0f & (uc >> 4)];
        chars[charsIndex++] = hexTable[0x0f & uc];
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