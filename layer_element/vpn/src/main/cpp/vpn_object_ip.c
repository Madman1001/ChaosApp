#ifndef _Included_vpn_ip_object
#define _Included_vpn_ip_object

#include <memory.h>
#include <stdlib.h>
#include "vpn_data.c"
#include "vpn_log.c"

#define IP_Packet struct IP_Packet
#define IP_STATUS_FAIL -1
#define IP_STATUS_SUCCESS 0

static void print_ip_packet(IP_Packet *packet) {

    TAG_D("ip >> version %d", packet->version);
    TAG_D("ip >> head_length %d", packet->head_length);
    TAG_D("ip >> type_of_service 0x%02x", packet->type_of_service);
    TAG_D("ip >> total_length %d", packet->total_length);
    TAG_D("ip >> identification 0x%04x", packet->identification);
    TAG_D("ip >> flag %x", packet->flag);
    TAG_D("ip >> offset_frag %d", packet->offset_frag);
    TAG_D("ip >> time_to_live %d", packet->time_to_live);
    TAG_D("ip >> upper_protocol %d", packet->upper_protocol);
    TAG_D("ip >> head_check_sum 0x%04x", packet->head_check_sum);

    unsigned int address = packet->source_ip_address;
    unsigned char sourceAddress[4];
    for (int i = 3; i >= 0; --i) {
        sourceAddress[i] = address & (unsigned int) 0xFF;
        address = address >> (unsigned int) 8;
    }
    TAG_D("ip >> source_ip_address %d.%d.%d.%d", sourceAddress[0], sourceAddress[1],
          sourceAddress[2], sourceAddress[3]);
    address = packet->target_ip_address;
    unsigned char targetAddress[4];
    for (int i = 3; i >= 0; --i) {
        targetAddress[i] = address & (unsigned int) 0xFF;
        address = address >> (unsigned int) 8;
    }
    TAG_D("ip >> target_ip_address %d.%d.%d.%d", targetAddress[0], targetAddress[1],
          targetAddress[2], targetAddress[3]);

//    if (packet->head_other_data != NULL){
//        TAG_E("head_other_data %s",packet->head_other_data);
//    }

//    if (packet->data != NULL){
//        TAG_E("data %s",packet->data);
//    }
}

static IP_Packet* generate_ip_packet(){
    IP_Packet* packet = malloc(sizeof(IP_Packet));
    packet->version = 4;
    packet->head_length = 5;
    packet->total_length = 20;
    packet->type_of_service = 0;
    packet->identification = 0;
    packet->flag = 0;
    packet->offset_frag = 0;
    packet->time_to_live = 0;
    packet->upper_protocol = 0;
    packet->head_check_sum = 0;
    packet->source_ip_address = 0;
    packet->target_ip_address = 0;
    packet->data = NULL;
    packet->head_other_data = NULL;
    return packet;
}

static int init_ip_packet(IP_Packet *ipPacket, unsigned const char *arrays) {
    if (arrays == NULL) {
        return IP_STATUS_FAIL;
    }

    ipPacket->version = ip_read_version(arrays);
    if (ipPacket->version == 0) {
        return IP_STATUS_FAIL;
    }

    ipPacket->head_length = ip_read_head_length(arrays);
    if (ipPacket->head_length == 0) {
        return IP_STATUS_FAIL;
    }

    ipPacket->type_of_service = ip_read_tos(arrays);

    ipPacket->total_length = ip_read_total_length(arrays);
    if (ipPacket->total_length == 0) {
        return IP_STATUS_FAIL;
    }

    ipPacket->identification = ip_read_identification(arrays);

    ipPacket->flag = ip_read_flag(arrays);

    ipPacket->offset_frag = ip_read_offset_frag(arrays);

    ipPacket->time_to_live = ip_read_ttl(arrays);

    ipPacket->upper_protocol = ip_read_upper_protocol(arrays);

    ipPacket->head_check_sum = ip_read_head_check_sum(arrays);

    ipPacket->source_ip_address = ip_read_source_ip_address(arrays);

    ipPacket->target_ip_address = ip_read_target_ip_address(arrays);

    int headLength = (int) ipPacket->head_length * 4;
    int dataLength = (int) ipPacket->total_length - headLength;

    ipPacket->data = NULL;
    if (headLength > 0) {
        ipPacket->data = malloc(dataLength * sizeof(char));
        ip_read_data(arrays, ipPacket->data, headLength, dataLength);
    }

    ipPacket->head_other_data = NULL;
    if (dataLength > 0) {
        ipPacket->head_other_data = malloc((headLength - 20) * sizeof(char));
        ip_read_other_head_fields(arrays, ipPacket->head_other_data, 20, (headLength - 20));
    }

    return IP_STATUS_SUCCESS;
}

static void release_ip_packet(IP_Packet *ipPacket) {
    if (ipPacket->data != NULL) {
        free(ipPacket->data);
        ipPacket->data = NULL;
    }

    if (ipPacket->head_other_data != NULL) {
        free(ipPacket->head_other_data);
        ipPacket->head_other_data = NULL;
    }
}

static unsigned char * ip_packet_to_binary(IP_Packet *ipPacket) {
    if (ipPacket->total_length <= 0) {
        return NULL;
    }
    unsigned char *packet_binary = malloc(ipPacket->total_length * sizeof(char));

    ip_write_version(packet_binary, ipPacket->version);
    ip_write_head_length(packet_binary, ipPacket->head_length);
    ip_write_tos(packet_binary, ipPacket->type_of_service);
    ip_write_total_length(packet_binary, ipPacket->total_length);
    ip_write_identification(packet_binary, ipPacket->identification);
    ip_write_flag(packet_binary, ipPacket->flag);
    ip_write_offset_frag(packet_binary, ipPacket->offset_frag);
    ip_write_ttl(packet_binary, ipPacket->time_to_live);
    ip_write_upper_protocol(packet_binary, ipPacket->upper_protocol);
    ip_write_head_check_sum(packet_binary, ipPacket->head_check_sum);
    ip_write_source_ip_address(packet_binary, ipPacket->source_ip_address);
    ip_write_target_ip_address(packet_binary, ipPacket->target_ip_address);

    if (ipPacket->head_length * 4 - 20 > 0 && ipPacket->head_other_data != NULL){
        ip_write_other_head_fields(packet_binary, ipPacket->head_other_data, 20,
                                   ipPacket->head_length * 4 - 20);
    }

    if (ipPacket->total_length - ipPacket->head_length * 4 > 0 && ipPacket->data != NULL){
        ip_write_data(packet_binary, ipPacket->data, ipPacket->head_length * 4,
                      ipPacket->total_length - ipPacket->head_length * 4);
    }

    return packet_binary;
}

/**
 * 校验和
 */
static unsigned short ip_check_sum(unsigned const short *data, int size){
    unsigned int sum = 0;
    while (size > 1){
        sum += *data++;
        size -= sizeof(short);
    }
    if (size){
        sum += *(unsigned char *)data;
    }
    while (sum >> 16){
        sum = (sum >> 16) + (sum & 0xffff);
    }
    return (unsigned short)(~sum);
}

static unsigned short get_ip_check_sum(unsigned short ip_head_length, unsigned char* ip_binary){
    char* checkData;

    checkData = malloc(ip_head_length);

    for (int i = 0; i < ip_head_length; i++){
        checkData[i] = ip_binary[i];
    }

    unsigned short cs = ip_check_sum((unsigned const short*) checkData, ip_head_length);

    free(checkData);

    return cs;
}

#endif