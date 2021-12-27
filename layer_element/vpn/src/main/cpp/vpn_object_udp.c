#ifndef _Included_vpn_udp_object
#define _Included_vpn_udp_object

#include <memory.h>
#include <stdlib.h>
#include "vpn_data.c"
#include "vpn_log.c"

#define UDP_Packet struct UDP_Packet
#define UDP_STATUS_FAIL -1
#define UDP_STATUS_SUCCESS 0

/**
* 读取udp源端口号
*/
static unsigned short udp_read_source_port(unsigned const char *data) {
    unsigned short source_port = (unsigned short)data[0];
    source_port = (source_port << 8) | (unsigned short)data[1];
    return source_port;
}

/**
* 写入udp源端口号
*/
static void udp_write_source_port(unsigned char *data, unsigned short source_port) {
    data[0] = (unsigned char)(source_port >> 8);
    data[1] = (unsigned char)(source_port & 0x00FF);
}

/**
* 读取udp目标端口号
*/
static unsigned short udp_read_target_port(unsigned const char *data) {
    unsigned short target_port = (unsigned short)data[2];
    target_port = (target_port << 8) | (unsigned short)data[3];
    return target_port;
}

/**
* 写入udp目标端口号
*/
static void udp_write_target_port(unsigned char *data, unsigned short target_port) {
    data[2] = (unsigned char)(target_port >> 8);
    data[3] = (unsigned char)(target_port & 0x00FF);
}

/**
* 读取udp长度
*/
static unsigned short udp_read_length(unsigned const char *data) {
    unsigned short length = (unsigned short)data[4];
    length = (length << 8) | (unsigned short)data[5];
    return length;
}

/**
* 写入udp长度
*/
static void udp_write_length(unsigned char *data, unsigned short length) {
    data[4] = (unsigned char)(length >> 8);
    data[5] = (unsigned char)(length & 0x00FF);
}

/**
* 读取udp校验和
*/
static unsigned short udp_read_check_sum(unsigned const char *data) {
    return ((unsigned short*)data)[3];
}

/**
* 写入udp校验和
*/
static void udp_write_check_sum(unsigned char *data, unsigned short check_sum) {
    ((unsigned short*)data)[3] = check_sum;
}

/**
* 读取udp数据
*/
static void udp_read_data(unsigned const char *data, unsigned char *udp_data, int offset, int length){
    int i = 0;
    for (; i < length; ++i) {
        udp_data[i] = data[offset + i];
    }
}

/**
* 写入udp数据
*/
static void udp_write_data(unsigned char *data,unsigned const char *udp_data, int offset, int length) {
    int i = 0;
    for (; i < length; ++i) {
        data[offset + i] = udp_data[i];
    }
}

/**
 * 校验和
 */
static unsigned short udp_check_sum(unsigned const short *data, int size){
    unsigned int sum = 0;
    while (size > 1){
        sum += *data++;
        sum = (sum >> 16) + (sum & 0xffff);
        size -= sizeof(short);
    }
    if (size){
        sum += *(unsigned char *)data;
        sum = (sum >> 16) + (sum & 0xffff);
    }
    while (sum >> 16){
        sum = (sum >> 16) + (sum & 0xffff);
    }
    return (unsigned short)(~sum);
}

static void print_udp_packet(UDP_Packet *packet){

    TAG_D("udp >> source port %d",packet->source_port);
    TAG_D("udp >> target port %d",packet->target_port);
    TAG_D("udp >> total length %d",packet->total_length);
    TAG_D("udp >> data length %d",packet->data_length);
    TAG_D("udp >> check sum 0x%04x",packet->check_sum);

    if (packet->data != NULL){
        TAG_D("udp >> data %s",packet->data);
    }
}

static UDP_Packet* generate_udp_packet(){
    UDP_Packet* packet = malloc(sizeof(UDP_Packet));
    packet->data = NULL;
    packet->check_sum = 0x0000;
    packet->target_port = 0x0000;
    packet->source_port = 0x0000;
    packet->total_length = 8;
    packet->data_length = 0;
    return packet;
}

/**
 * 初始化udp_packet结构体
 * @param udpPacket 结构体指针
 * @param arrays tcp数据
 * @param total_length 总长度
 * @return 初始化结果
 */
static int init_udp_packet(UDP_Packet *udpPacket, unsigned const char* arrays, int total_length) {
    if (arrays == NULL){
        return UDP_STATUS_FAIL;
    }

    udpPacket->source_port = udp_read_source_port(arrays);

    udpPacket->target_port = udp_read_target_port(arrays);

    udpPacket->total_length = udp_read_length(arrays);

    udpPacket->check_sum = udp_read_check_sum(arrays);

    udpPacket->data_length = total_length - (unsigned short)8;

    udpPacket->data = NULL;
    if (udpPacket->data_length > 0){
        udpPacket->data = malloc((udpPacket->data_length) * sizeof(char));
        udp_read_data(arrays, udpPacket->data, 8, udpPacket->data_length);
    }
    return UDP_STATUS_SUCCESS;
}

static void release_udp_packet(UDP_Packet *udpPacket) {
    if(udpPacket->data != NULL){
        free(udpPacket->data);
        udpPacket->data = NULL;
        udpPacket->data_length = 0;
    }
}

static unsigned char * udp_packet_to_binary(UDP_Packet* udpPacket) {
    if (udpPacket->total_length <= 0) {
        return NULL;
    }

    unsigned char *packet_binary = malloc(udpPacket->total_length * sizeof(char));

    udp_write_source_port(packet_binary,udpPacket->source_port);
    udp_write_target_port(packet_binary,udpPacket->target_port);
    udp_write_length(packet_binary,udpPacket->total_length);

    udp_write_check_sum(packet_binary,udpPacket->check_sum);
    if (udpPacket->total_length - 8 > 0 && udpPacket->data != NULL){
        udp_write_data(packet_binary,udpPacket->data,8, udpPacket->total_length - 4);
    }

    return packet_binary;
}

static unsigned short get_udp_check_sum(unsigned int sourceAddress, unsigned int targetAddress, unsigned short udp_total_length, unsigned char* udp_binary){
    char* checkData;

    int size = 12 + udp_total_length;

    checkData = malloc(size);

    checkData[0] = (unsigned char)((sourceAddress >> 24) & 0x000000FF);
    checkData[1] = (unsigned char)((sourceAddress >> 16) & 0x000000FF);
    checkData[2] = (unsigned char)((sourceAddress >> 8) & 0x000000FF);
    checkData[3] = (unsigned char)(sourceAddress & 0x000000FF);

    checkData[4] = (unsigned char)((targetAddress >> 24) & 0x000000FF);
    checkData[5] = (unsigned char)((targetAddress >> 16) & 0x000000FF);
    checkData[6] = (unsigned char)((targetAddress >> 8) & 0x000000FF);
    checkData[7] = (unsigned char)(targetAddress & 0x000000FF);

    checkData[8] = 0x00;
    checkData[9] = 0x11;

    checkData[10] = (unsigned char)(udp_total_length >> 8);
    checkData[11] = (unsigned char)(udp_total_length & 0x00FF);

    for (int i = 0; i < udp_total_length; i++){
        checkData[12 + i] = udp_binary[i];
    }

    unsigned short cs = udp_check_sum((unsigned const short*) checkData, size);

    free(checkData);

    return cs;
}

#endif