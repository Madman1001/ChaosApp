#ifndef _Included_vpn_ip_object
#define _Included_vpn_ip_object

#include <jni.h>
#include <stdlib.h>
#include <memory.h>
#include <android/log.h>

#include "vpn_data.h"
#include "vpn_data_utils.c"

#define  TAG_E(...) __android_log_print(ANDROID_LOG_ERROR, "NativeLog", __VA_ARGS__)
#define IP_Packet struct IP_Packet

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeInit
        (JNIEnv *env, jobject jobj, jbyteArray jba) {
    IP_Packet ipPacket;

    //获取数据报长度
    int len = (*env)->GetArrayLength(env, jba);
    char *arrays = (char *) malloc(len * sizeof(char));

    ipPacket.version = readVersion(arrays);

    ipPacket.head_length = readHeadLength(arrays);

    ipPacket.type_of_service = readTOS(arrays);

    ipPacket.total_length = readTotalLength(arrays);

    ipPacket.identification = readIdentification(arrays);

    ipPacket.flag = readFlag(arrays);

    ipPacket.offset_frag = readOffsetFrag(arrays);

    ipPacket.time_to_live = readTTL(arrays);

    ipPacket.upper_protocol = readUpperProtocol(arrays);

    ipPacket.head_check_sum = readHeadCheckSum(arrays);

    ipPacket.source_ip_address = malloc(4 * sizeof(unsigned char));
    readSourceIpAddress(arrays, ipPacket.source_ip_address);

    ipPacket.target_ip_address = malloc(4 * sizeof(unsigned char));
    readTargetIpAddress(arrays, ipPacket.target_ip_address);

    int headLength = (int)ipPacket.head_length * 4;
    int dataLength = (int)ipPacket.total_length - headLength;
    ipPacket.data = malloc(dataLength * sizeof(unsigned char));
    readData(arrays, ipPacket.data, headLength, dataLength);

    ipPacket.other_head_fields = malloc((headLength - 20) * sizeof(unsigned char));
    readOtherHeadFields(arrays,ipPacket.other_head_fields, 20, (headLength - 20));

    TAG_E("%s:%d", "ip data version", ipPacket.version);

    TAG_E("%s:%d", "ip data head length", ipPacket.head_length);
}

#endif