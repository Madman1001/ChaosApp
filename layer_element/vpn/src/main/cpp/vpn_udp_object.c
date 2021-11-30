#ifndef _Included_vpn_udp_object
#define _Included_vpn_udp_object

#include "vpn_data.h"
#include "vpn_log.c"
#include "vpn_udp_data_utils.c"

#define UDP_Packet struct UDP_Packet
#define UDP_STATUS_FAIL -1
#define UDP_STATUS_SUCCESS 0

void print_udp_packet(UDP_Packet *packet){

    TAG_E("source port %d",packet->source_port);
    TAG_E("target port %d",packet->target_port);
    TAG_E("udp length %d",packet->udp_length);
    TAG_E("udp check sum %d",packet->udp_check_sum);

    if (packet->data != NULL){
        TAG_E("data %s",packet->data);
    }
}

static int init_ip_packet(UDP_Packet *udpPacket, const char* arrays) {
    if (arrays == NULL){
        return UDP_STATUS_FAIL;
    }

    udpPacket->source_port = udp_read_source_port(arrays);

    udpPacket->target_port = udp_read_target_port(arrays);

    udpPacket->udp_length = udp_read_length(arrays);

    udpPacket->udp_check_sum = udp_read_check_sum(arrays);

    udpPacket->data = NULL;
    if (udpPacket-> udp_length - 8 > 0){
        udpPacket->data = malloc((udpPacket-> udp_length - 8) * sizeof(char));
        ip_read_data(arrays, udpPacket->data, 8, (udpPacket-> udp_length - 8));
    }

    print_udp_packet(udpPacket);

    return UDP_STATUS_SUCCESS;
}

#endif