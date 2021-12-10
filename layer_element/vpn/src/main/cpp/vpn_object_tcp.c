#ifndef _Included_vpn_tcp_object
#define _Included_vpn_tcp_object

#include <memory.h>
#include <stdlib.h>
#include "vpn_data.c"
#include "vpn_log.c"
#include "vpn_java_utils.c"

#define TCP_Packet struct TCP_Packet
#define TCP_STATUS_FAIL -1
#define TCP_STATUS_SUCCESS 0

static void print_tcp_packet(TCP_Packet *packet){

    TAG_D("tcp >> source port %d",packet->source_port);
    TAG_D("tcp >> target port %d",packet->target_port);
    TAG_D("tcp >> serial number 0x%08x",packet->serial_number);
    TAG_D("tcp >> verify serial number 0x%08x",packet->verify_serial_number);
    TAG_D("tcp >> head length %d",packet->head_length);
    TAG_D("tcp >> control sign 0d%s",charToBinary(packet->control_sign));
    TAG_D("tcp >> window size %d",packet->window_size);
    TAG_D("tcp >> check sum 0x%04x",packet->check_sum);
    TAG_D("tcp >> urgent pointer 0x%04x",packet->urgent_pointer);

    if (packet->head_other_data != NULL){
        TAG_D("tcp >> head other option %s",packet->head_other_data);
    }

    if (packet->data != NULL){
        TAG_D("tcp >> data %s",packet->data);
    }
}

/**
 * 初始化tcp_packet结构体
 * @param tcpPacket 结构体指针
 * @param arrays tcp数据
 * @param total_length 总长度
 * @return 初始化结果
 */
static int init_tcp_packet(TCP_Packet *tcpPacket, unsigned const char* arrays, int total_length) {
    if (arrays == NULL || total_length < 20){
        return TCP_STATUS_FAIL;
    }

    tcpPacket->total_length = total_length;

    tcpPacket->source_port = tcp_read_source_port(arrays);

    tcpPacket->target_port = tcp_read_target_port(arrays);

    tcpPacket->serial_number = tcp_read_serial_number(arrays);

    tcpPacket->verify_serial_number = tcp_read_verify_serial_number(arrays);

    tcpPacket->head_length = tcp_read_head_length(arrays);

    tcpPacket->control_sign = tcp_read_control_sign(arrays);

    tcpPacket->window_size = tcp_read_window_size(arrays);

    tcpPacket->check_sum = tcp_read_check_sum(arrays);

    tcpPacket->urgent_pointer = tcp_read_urgent_pointer(arrays);

    tcpPacket->head_other_data = NULL;
    int option_length = tcpPacket->head_length - 20;
    if (option_length > 0){
        tcpPacket->head_other_data = malloc(option_length * sizeof(char));
        tcp_read_head_other_option(arrays, tcpPacket->head_other_data, 20, option_length);
    }

    tcpPacket->data = NULL;
    int data_length = total_length - (tcpPacket->head_length * 4);
    if (data_length > 0){
        tcpPacket->data = malloc(data_length * sizeof(char));
        tcp_read_data(arrays, tcpPacket->data, tcpPacket->head_length * 4, data_length);
    }

    return TCP_STATUS_SUCCESS;
}

static void release_tcp_packet(TCP_Packet *tcpPacket) {
    if(tcpPacket->data != NULL){
        free(tcpPacket->data);
        tcpPacket->data = NULL;
    }

    if (tcpPacket->head_other_data != NULL){
        free(tcpPacket->head_other_data);
        tcpPacket->head_other_data = NULL;
    }
}

#endif