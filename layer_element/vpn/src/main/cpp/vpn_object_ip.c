#ifndef _Included_vpn_ip_object
#define _Included_vpn_ip_object

#include <memory.h>
#include <stdlib.h>
#include "vpn_data.c"
#include "vpn_log.c"

#define IP_Packet struct IP_Packet
#define IP_STATUS_FAIL -1
#define IP_STATUS_SUCCESS 0

static void print_ip_packet(IP_Packet *packet){

    TAG_E("ip >> version %d",packet->version);
    TAG_E("ip >> head_length %d",packet->head_length);
    TAG_E("ip >> type_of_service 0x%02x",packet->type_of_service);
    TAG_E("ip >> total_length %d",packet->total_length);
    TAG_E("ip >> identification 0x%04x",packet->identification);
    TAG_E("ip >> flag %x",packet->flag);
    TAG_E("ip >> offset_frag %d",packet->offset_frag);
    TAG_E("ip >> time_to_live %d",packet->time_to_live);
    TAG_E("ip >> upper_protocol %d",packet->upper_protocol);
    TAG_E("ip >> head_check_sum 0x%04x",packet->head_check_sum);

    unsigned char* sourceAddress = packet->source_ip_address;
    TAG_E("ip >> source_ip_address %d.%d.%d.%d",sourceAddress[0],sourceAddress[1],sourceAddress[2],sourceAddress[3]);
    unsigned char* targetAddress = packet->target_ip_address;
    TAG_E("ip >> target_ip_address %d.%d.%d.%d",targetAddress[0],targetAddress[1],targetAddress[2],targetAddress[3]);

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

    ipPacket->head_length = ip_read_head_length(arrays);
    if (ipPacket->head_length == 0){
        return IP_STATUS_FAIL;
    }

    ipPacket->type_of_service = ip_read_tos(arrays);

    ipPacket->total_length = ip_read_total_length(arrays);
    if (ipPacket->total_length == 0){
        return IP_STATUS_FAIL;
    }

    ipPacket->identification = ip_read_identification(arrays);

    ipPacket->flag = ip_read_flag(arrays);

    ipPacket->offset_frag = ip_read_offset_frag(arrays);

    ipPacket->time_to_live = ip_read_ttl(arrays);

    ipPacket->upper_protocol = ip_read_upper_protocol(arrays);

    ipPacket->head_check_sum = ip_read_head_check_sum(arrays);

    ipPacket->source_ip_address = malloc(4 * sizeof(unsigned char));
    ip_read_source_ip_address(arrays, ipPacket->source_ip_address);

    ipPacket->target_ip_address = malloc(4 * sizeof(unsigned char));
    ip_read_target_ip_address(arrays, ipPacket->target_ip_address);

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

    print_ip_packet(ipPacket);

    return IP_STATUS_SUCCESS;
}

#endif