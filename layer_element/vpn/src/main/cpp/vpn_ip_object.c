#ifndef _Included_vpn_ip_object
#define _Included_vpn_ip_object

#include <jni.h>
#include <stdlib.h>
#include <memory.h>
#include <android/log.h>

#include "vpn_data.h"
#include "vpn_data_utils.c"
#include "vpn_java_utils.c"

#define  TAG_E(...) __android_log_print(ANDROID_LOG_ERROR, "NativeLog", __VA_ARGS__)
#define IP_Packet struct IP_Packet

void printPacket(IP_Packet *packet){

    TAG_E("void* %d jlong %d",sizeof(void*), sizeof(jlong));

    TAG_E("version %d",packet->version);
    TAG_E("head_length %d",packet->head_length);
    TAG_E("type_of_service %d",packet->type_of_service);
    TAG_E("total_length %d",packet->total_length);
    TAG_E("identification %d",packet->identification);
    TAG_E("flag %d",packet->flag);
    TAG_E("offset_frag %d",packet->offset_frag);
    TAG_E("time_to_live %d",packet->time_to_live);
    TAG_E("upper_protocol %d",packet->upper_protocol);
    TAG_E("head_check_sum %d",packet->head_check_sum);

    unsigned char* sourceAddress = packet->source_ip_address;
    TAG_E("source_ip_address %d.%d.%d.%d",sourceAddress[0],sourceAddress[1],sourceAddress[2],sourceAddress[3]);
    unsigned char* targetAddress = packet->target_ip_address;
    TAG_E("target_ip_address %d.%d.%d.%d",targetAddress[0],targetAddress[1],targetAddress[2],targetAddress[3]);

//    if (packet->other_head_fields != NULL){
//        TAG_E("other_head_fields %s",packet->other_head_fields);
//    }

//    if (packet->data != NULL){
//        TAG_E("data %s",packet->data);
//    }
}

JNIEXPORT void JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeInit
        (JNIEnv *env, jobject jobj, jbyteArray jba) {
    IP_Packet *ipPacket = malloc(sizeof(IP_Packet));

    //获取数据报长度
    int len = (*env)->GetArrayLength(env, jba);
    if (len == 0){
        return;
    }

    char *arrays = (char *) malloc(len * sizeof(char));
    (*env)->GetByteArrayRegion(env, jba, 0, len, (jbyte*)arrays);

    if (arrays == NULL){
        return;
    }

    ipPacket->version = readVersion(arrays);
    if (ipPacket->version == 0){
        return;
    }
    TAG_E("version %d",ipPacket->version);

    ipPacket->head_length = readHeadLength(arrays);
    TAG_E("head_length %d",ipPacket->head_length);
    if (ipPacket->head_length == 0){
        return;
    }

    ipPacket->type_of_service = readTOS(arrays);
    TAG_E("type_of_service %d",ipPacket->type_of_service);

    ipPacket->total_length = readTotalLength(arrays);
    TAG_E("total_length %d",ipPacket->total_length);
    if (ipPacket->total_length == 0){
        return;
    }

    ipPacket->identification = readIdentification(arrays);
    TAG_E("identification %d",ipPacket->identification);

    ipPacket->flag = readFlag(arrays);
    TAG_E("flag %d",ipPacket->flag);

    ipPacket->offset_frag = readOffsetFrag(arrays);
    TAG_E("offset_frag %d",ipPacket->offset_frag);

    ipPacket->time_to_live = readTTL(arrays);
    TAG_E("time_to_live %d",ipPacket->time_to_live);

    ipPacket->upper_protocol = readUpperProtocol(arrays);
    TAG_E("upper_protocol %d",ipPacket->upper_protocol);

    ipPacket->head_check_sum = readHeadCheckSum(arrays);
    TAG_E("head_check_sum %d",ipPacket->head_check_sum);

    ipPacket->source_ip_address = malloc(4 * sizeof(unsigned char));
    readSourceIpAddress(arrays, ipPacket->source_ip_address);
    unsigned char* sourceAddress = ipPacket->source_ip_address;
    TAG_E("source_ip_address %d.%d.%d.%d",sourceAddress[0],sourceAddress[1],sourceAddress[2],sourceAddress[3]);

    ipPacket->target_ip_address = malloc(4 * sizeof(unsigned char));
    readTargetIpAddress(arrays, ipPacket->target_ip_address);
    unsigned char* targetAddress = ipPacket->target_ip_address;
    TAG_E("target_ip_address %d.%d.%d.%d",targetAddress[0],targetAddress[1],targetAddress[2],targetAddress[3]);

    int headLength = (int)ipPacket->head_length * 4;
    int dataLength = (int)ipPacket->total_length - headLength;

    ipPacket->data = NULL;
    if (headLength > 0){
        ipPacket->data = malloc(dataLength * sizeof(char) + 1);
        ipPacket->data[dataLength * sizeof(char)] = '\0';
        readData(arrays, ipPacket->data, headLength, dataLength);
    }
//    if (ipPacket->other_head_fields != NULL){
//        TAG_E("other_head_fields %s",ipPacket->other_head_fields);
//    }

    TAG_E("data over");

    ipPacket->other_head_fields = NULL;
//    if (dataLength > 0){
//        ipPacket->other_head_fields = malloc((headLength - 20) * sizeof(char) + 1);
//        ipPacket->other_head_fields[(headLength - 20) * sizeof(char)] = '\0';
//        readOtherHeadFields(arrays,ipPacket->other_head_fields, 20, (headLength - 20));
//    }
//    if (ipPacket->data != NULL){
//        TAG_E("data %s",ipPacket->data);
//    }

    TAG_E("other_head_fields over");


    jclass ip_object_class = (*env)->GetObjectClass(env,jobj);

    jfieldID ip_object_jfieldId = (*env)->GetFieldID(env,ip_object_class,"mPacketRef","I");

    (*env)->SetIntField(env,jobj,ip_object_jfieldId,(jint)ipPacket);

    TAG_E("init over");
}

JNIEXPORT jstring JNICALL Java_com_lhr_vpn_protocol_IPPacket_nativeGetData
        (JNIEnv *env, jobject jobj, jint jpacket) {
    IP_Packet* nativeIp = (IP_Packet*)jpacket;
    printPacket(nativeIp);
    return charTojstring(env,"test");
}

#endif