#ifndef _Included_vpn_ip_object
#define _Included_vpn_ip_object

#include "vpn_data.h"
#include "vpn_log.c"
#include "vpn_ip_data_utils.c"

#define IP_Packet struct IP_Packet
#define IP_STATUS_FAIL -1
#define IP_STATUS_SUCCESS 0

void print_ip_packet(IP_Packet *packet){

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

static int init_ip_packet(IP_Packet *ipPacket, const char* arrays) {
    if (arrays == NULL){
        return IP_STATUS_FAIL;
    }

    ipPacket->version = ip_read_version(arrays);
    if (ipPacket->version == 0){
        return IP_STATUS_FAIL;
    }
    TAG_E("version %d",ipPacket->version);

    ipPacket->head_length = ip_read_head_length(arrays);
    TAG_E("head_length %d",ipPacket->head_length);
    if (ipPacket->head_length == 0){
        return IP_STATUS_FAIL;
    }

    ipPacket->type_of_service = ip_read_tos(arrays);
    TAG_E("type_of_service %d",ipPacket->type_of_service);

    ipPacket->total_length = ip_read_total_length(arrays);
    TAG_E("total_length %d",ipPacket->total_length);
    if (ipPacket->total_length == 0){
        return IP_STATUS_FAIL;
    }

    ipPacket->identification = ip_read_identification(arrays);
    TAG_E("identification %d",ipPacket->identification);

    ipPacket->flag = ip_read_flag(arrays);
    TAG_E("flag %d",ipPacket->flag);

    ipPacket->offset_frag = ip_read_offset_frag(arrays);
    TAG_E("offset_frag %d",ipPacket->offset_frag);

    ipPacket->time_to_live = ip_read_ttl(arrays);
    TAG_E("time_to_live %d",ipPacket->time_to_live);

    ipPacket->upper_protocol = ip_read_upper_protocol(arrays);
    TAG_E("upper_protocol %d",ipPacket->upper_protocol);

    ipPacket->head_check_sum = ip_read_head_check_sum(arrays);
    TAG_E("head_check_sum %d",ipPacket->head_check_sum);

    ipPacket->source_ip_address = malloc(4 * sizeof(unsigned char));
    ip_read_source_ip_address(arrays, ipPacket->source_ip_address);
    unsigned char* sourceAddress = ipPacket->source_ip_address;
    TAG_E("source_ip_address %d.%d.%d.%d",sourceAddress[0],sourceAddress[1],sourceAddress[2],sourceAddress[3]);

    ipPacket->target_ip_address = malloc(4 * sizeof(unsigned char));
    ip_read_target_ip_address(arrays, ipPacket->target_ip_address);
    unsigned char* targetAddress = ipPacket->target_ip_address;
    TAG_E("target_ip_address %d.%d.%d.%d",targetAddress[0],targetAddress[1],targetAddress[2],targetAddress[3]);

    int headLength = (int)ipPacket->head_length * 4;
    int dataLength = (int)ipPacket->total_length - headLength;

    ipPacket->data = NULL;
    if (headLength > 0){
        ipPacket->data = malloc(dataLength * sizeof(char));
        ip_read_data(arrays, ipPacket->data, headLength, dataLength);
    }

    ipPacket->other_head_fields = NULL;
    if (dataLength > 0){
        ipPacket->other_head_fields = malloc((headLength - 20) * sizeof(char));
        ip_read_other_head_fields(arrays, ipPacket->other_head_fields, 20, (headLength - 20));
    }
    TAG_E("init over");

    return IP_STATUS_SUCCESS;
}

#endif