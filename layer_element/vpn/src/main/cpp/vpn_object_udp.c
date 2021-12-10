#ifndef _Included_vpn_udp_object
#define _Included_vpn_udp_object

#include <memory.h>
#include <stdlib.h>
#include "vpn_data.c"
#include "vpn_log.c"

#define UDP_Packet struct UDP_Packet
#define UDP_STATUS_FAIL -1
#define UDP_STATUS_SUCCESS 0

static void print_udp_packet(UDP_Packet *packet){

    TAG_E("udp >> source port %d",packet->source_port);
    TAG_E("udp >> target port %d",packet->target_port);
    TAG_E("udp >> total length %d",packet->udp_total_length);
    TAG_E("udp >> data length %d",packet->udp_data_length);
    TAG_E("udp >> check sum 0x%04x",packet->udp_check_sum);

    if (packet->data != NULL){
        TAG_E("udp >> data %s",packet->data);
    }
}

/**
 * 初始化udp_packet结构体
 * @param udpPacket 结构体指针
 * @param arrays tcp数据
 * @param total_length 总长度
 * @return 初始化结果
 */
static int init_udp_packet(UDP_Packet *udpPacket, const char* arrays, int total_length) {
    if (arrays == NULL){
        return UDP_STATUS_FAIL;
    }

    udpPacket->source_port = udp_read_source_port(arrays);

    udpPacket->target_port = udp_read_target_port(arrays);

    udpPacket->udp_total_length = udp_read_length(arrays);

    udpPacket->udp_check_sum = udp_read_check_sum(arrays);

    udpPacket->udp_data_length = total_length - (unsigned short)8;

    udpPacket->data = NULL;
    if (udpPacket->udp_data_length > 0){
        udpPacket->data = malloc((udpPacket->udp_data_length) * sizeof(char));
        udp_read_data(arrays, udpPacket->data, 8, udpPacket->udp_data_length);
    }
    return UDP_STATUS_SUCCESS;
}

static void release_udp_packet(UDP_Packet *udpPacket) {
    if(udpPacket->data != NULL){
        free(udpPacket->data);
        udpPacket->data = NULL;
    }
}

#endif