#ifndef _Included_vpn_tcp_object
#define _Included_vpn_tcp_object

#include <memory.h>
#include <stdlib.h>
#include "vpn_data.c"
#include "vpn_log.c"
#include "vpn_java_utils.c"

#define TCP_Packet struct TCP_Packet
#define TCP_Option struct TCP_Option
#define TCP_STATUS_FAIL -1
#define TCP_STATUS_SUCCESS 0


/**
* 读取tcp源端口号
*/

static unsigned short tcp_read_source_port(unsigned const char *data) {
    unsigned short source_port = (unsigned short) data[0];
    source_port = (source_port << 8) | (unsigned short) data[1];
    return source_port;
}

/**
* 写入tcp源端口号
*/
static void tcp_write_source_port(unsigned char *data, unsigned short source_port) {
    data[0] = (unsigned char)(source_port >> 8);
    data[1] = (unsigned char)(source_port & 0x00FF);
}

/**
* 读取tcp目标端口号
*/
static unsigned short tcp_read_target_port(unsigned const char *data) {
    unsigned short target_port = (unsigned short) data[2];
    target_port = (target_port << 8) | (unsigned short) data[3];
    return target_port;
}

/**
* 写入tcp目标端口号
*/
static void tcp_write_target_port(unsigned char *data, unsigned short target_port) {
    data[2] = (unsigned char)(target_port >> 8);
    data[3] = (unsigned char)(target_port & 0x00FF);
}

/**
* 读取tcp序号
*/
static unsigned int tcp_read_serial_number(unsigned const char *data) {
    unsigned int serial_number = ((int *) data)[1];
    return serial_number;
}

/**
* 写入tcp序号
*/
static void tcp_write_serial_number(unsigned const char *data, unsigned int serial_number) {
    ((int *) data)[1] = serial_number;
}

/**
* 读取tcp确认序号
*/
static unsigned int tcp_read_verify_serial_number(unsigned const char *data) {
    unsigned int verify_serial_number = ((int *) data)[2];
    return verify_serial_number;
}

/**
* 写入tcp确认序号
*/
static void tcp_write_verify_serial_number(unsigned const char *data, unsigned int verify_serial_number) {
    ((int *) data)[2] = verify_serial_number;
}

/**
* 读取tcp首部长度
*/
static unsigned char tcp_read_head_length(unsigned const char *data) {
    unsigned char head_length = data[12];
    return head_length >> 4;
}

/**
* 写入tcp首部长度
*/
static void tcp_write_head_length(unsigned char *data, unsigned char head_length) {
    data[12] = head_length << 4;
}

/**
 * 读取tcp控制位
 */
static unsigned char tcp_read_control_sign(unsigned const char *data) {
    unsigned char control_sign = data[13];
    return control_sign;
}

/**
 * 写入tcp控制位
 */
static void tcp_write_control_sign(unsigned char *data, unsigned char control_sign) {
    data[13] = control_sign;
}

/**
 * 读取tcp窗口大小
 */
static unsigned short tcp_read_window_size(unsigned const char *data) {
    unsigned short window_size = (unsigned short)data[14];
    window_size = (window_size << 8) | (unsigned short)data[15];
    return window_size;
}

/**
 * 写入tcp窗口大小
 */
static void tcp_write_window_size(unsigned char *data, unsigned short window_size) {
    data[14] = (unsigned char)(window_size >> 8);
    data[15] = (unsigned char)(window_size & 0x00FF);
}

/**
 * 读取tcp校验和
 */
static unsigned short tcp_read_check_sum(unsigned const char *data) {
    return ((unsigned short *)data)[8];
}

/**
 * 写入tcp校验和
 */
static void tcp_write_check_sum(unsigned char *data, unsigned short check_sum) {
    ((unsigned short *)data)[8] = check_sum;
}

/**
 * 读取tcp紧急指针
 */
static unsigned short tcp_read_urgent_pointer(unsigned const char *data) {
    return ((unsigned short *)data)[9];
}

/**
 * 写入tcp紧急指针
 */
static void tcp_write_urgent_pointer(unsigned char *data, unsigned short urgent_pointer) {
    ((unsigned short *)data)[9] = urgent_pointer;
}

/**
* 读取tcp其它选项
*/
static TCP_Option* tcp_read_options(unsigned const char *data, int offset, int length, unsigned int *options_len) {
    unsigned int len = 0;
    for  (int i = 0; i < length;){
        if (data[offset + i] == (unsigned char)0x00 || data[offset + i] == (unsigned char)0x01){
            i++;
        } else{
            int total_length = (unsigned char)data[offset + i + 1];
            i += total_length;
        }
        len++;
    }
    *options_len = len;

    TCP_Option* options = malloc(sizeof(TCP_Option) * len);

    int index = 0;
    for (int i = 0; i < length;) {
        TCP_Option *tcpOption = &options[index];
        tcpOption->kind = (unsigned char)data[offset + i++];
        if (tcpOption->kind == (unsigned char)0x00 || tcpOption->kind == (unsigned char)0x01){
            tcpOption->length = 1;
            tcpOption->data = NULL;
        } else {
            tcpOption->length = (unsigned char)data[offset + i++];
            if (tcpOption->length - 2 > 0){
                tcpOption->data = malloc(tcpOption->length);
                for (int j = 0; j < tcpOption->length - 2; j++){
                    tcpOption->data[j] = data[offset + i++];
                }
            } else {
                tcpOption->data = NULL;
            }
        }
        index++;
    }
    return options;
}

/**
* 写入tcp其它选项
*/
static void tcp_write_options(unsigned char *data, int offset,int data_length, TCP_Option *options, unsigned int options_len) {
    int index = 0;
    for (int i = 0; i < options_len; ++i) {
        TCP_Option * tcpOption = &options[i];
        data[offset + index++] = tcpOption->kind;
        if (tcpOption->kind == (unsigned char)0x00 || tcpOption->kind == (unsigned char)0x01){
            continue;
        }
        data[offset + index++] = tcpOption->length;
        if (tcpOption->length - 2 <= 0){
            continue;
        }
        int len = tcpOption->length - 2;
        for (int j = 0; j < len; ++j) {
            data[offset + index++] = tcpOption->data[j];
        }
    }
    while (index < data_length){
        data[offset + index++] = (unsigned char)0x00;
    }
}

/**
* 读取tcp数据
*/
static void tcp_read_data(unsigned const char *data, unsigned char *tcp_data, int offset, int length) {
    int i = 0;
    for (; i < length; ++i) {
        tcp_data[i] = data[offset + i];
    }
}

/**
* 写入tcp数据
*/
static void tcp_write_data(unsigned char *data,unsigned const char *tcp_data, int offset, int length) {
    int i = 0;
    for (; i < length; ++i) {
        data[offset + i] = tcp_data[i];
    }
}

/**
 * 校验和
 */
static unsigned short tcp_check_sum(unsigned const short *data, int size){
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

static void print_tcp_packet(TCP_Packet *packet){

    TAG_D("tcp >> source port %d",packet->source_port);
    TAG_D("tcp >> target port %d",packet->target_port);
    TAG_D("tcp >> serial number %u",packet->serial_number);
    TAG_D("tcp >> verify serial number %u",packet->verify_serial_number);
    TAG_D("tcp >> head length %d",packet->head_length);
    TAG_D("tcp >> total length %d",packet->total_length);
    TAG_D("tcp >> control sign 0b%s",charToBinary(packet->control_sign));
    TAG_D("tcp >> window size %ud",packet->window_size);
    TAG_D("tcp >> check sum 0x%04x",packet->check_sum);
    TAG_D("tcp >> urgent pointer 0x%04x",packet->urgent_pointer);

    TAG_D("tcp >> option size %d",packet->options_length);
    if (packet->options_length > 0 && packet->options != NULL){
        TAG_D("[");
        for (int i = 0; i < packet->options_length; ++i) {
            TCP_Option option = packet->options[i];
            TAG_D("       kind: %d, length: %d",option.kind, option.length);
//            if (option.length - 2 > 0 && option.data != NULL){
//                for (int j = 0; j < option.length - 2; ++j) {
//                    TAG_D("%s",charToBinary(option.data[j]));
//                }
//            }
        }
        TAG_D("]");
    }

    if (packet->data_length > 0 && packet->data != NULL){
        TAG_D("tcp >> data %s",packet->data);
    }
}

static TCP_Packet* generate_tcp_packet(){
    TCP_Packet* packet = malloc(sizeof(TCP_Packet));
    packet->source_port = 0x0000;
    packet->target_port = 0x0000;
    packet->serial_number = 0x00000000;
    packet->verify_serial_number = 0x00000000;
    packet->head_length = 5;
    packet->data_length = 0;
    packet->options_length = 0;
    packet->keep_position = 0x00;
    packet->control_sign = 0x00;
    packet->window_size = 0xFFFF;
    packet->check_sum = 0x0000;
    packet->urgent_pointer = 0x0000;
    packet->total_length = 20;
    packet->options = NULL;
    packet->data = NULL;
    return packet;
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

    tcpPacket->data_length = tcpPacket->total_length - tcpPacket->head_length * 4;

    tcpPacket->control_sign = tcp_read_control_sign(arrays);

    tcpPacket->window_size = tcp_read_window_size(arrays);

    tcpPacket->check_sum = tcp_read_check_sum(arrays);

    tcpPacket->urgent_pointer = tcp_read_urgent_pointer(arrays);

    int option_length = tcpPacket->head_length * 4 - 20;
    if (option_length > 0){
        tcpPacket->options = tcp_read_options(arrays, 20, option_length, &(tcpPacket->options_length));
    }

    int data_length = total_length - (tcpPacket->head_length * 4);
    if (data_length > 0){
        tcpPacket->data = malloc(data_length * sizeof(char));
        tcp_read_data(arrays, tcpPacket->data, tcpPacket->head_length * 4, data_length);
    }

    return TCP_STATUS_SUCCESS;
}

static void release_tcp_option(TCP_Packet *tcpPacket){
    if (tcpPacket->options != NULL && tcpPacket->options_length > 0){
        for (int i = 0; i < tcpPacket->options_length; ++i) {
            free(tcpPacket->options[i].data);
        }
        free(tcpPacket->options);
        tcpPacket->options = NULL;
        tcpPacket->options_length = 0;
        tcpPacket->head_length = 5;
        tcpPacket->total_length = tcpPacket->head_length * 4 + tcpPacket->data_length;
    }
}

static void release_tcp_data(TCP_Packet *tcpPacket){
    if(tcpPacket->data != NULL){
        free(tcpPacket->data);
        tcpPacket->data = NULL;
        tcpPacket->data_length = 0;
        tcpPacket->total_length = tcpPacket->head_length * 4;
    }
}

static void release_tcp_packet(TCP_Packet *tcpPacket) {
    release_tcp_option(tcpPacket);
    release_tcp_data(tcpPacket);
}

static unsigned char * tcp_packet_to_binary(TCP_Packet* tcpPacket) {
    if (tcpPacket->total_length <= 0) {
        return NULL;
    }
    unsigned char *packet_binary = malloc(tcpPacket->total_length * sizeof(char));

    tcp_write_source_port(packet_binary,tcpPacket->source_port);
    tcp_write_target_port(packet_binary,tcpPacket->target_port);
    tcp_write_serial_number(packet_binary,tcpPacket->serial_number);
    tcp_write_verify_serial_number(packet_binary,tcpPacket->verify_serial_number);
    tcp_write_head_length(packet_binary,tcpPacket->head_length);
    tcp_write_control_sign(packet_binary,tcpPacket->control_sign);
    tcp_write_window_size(packet_binary,tcpPacket->window_size);
    tcp_write_check_sum(packet_binary,tcpPacket->check_sum);
    tcp_write_urgent_pointer(packet_binary,tcpPacket->urgent_pointer);

    if (tcpPacket->head_length - 5 > 0 && tcpPacket->options != NULL){
        tcp_write_options(packet_binary, 20, (tcpPacket->head_length - 5) * 4, tcpPacket->options, tcpPacket->options_length);
    }

    if (tcpPacket->data_length > 0 && tcpPacket->data != NULL){
        tcp_write_data(packet_binary,tcpPacket->data,tcpPacket->head_length * 4, tcpPacket->data_length);
    }

    return packet_binary;
}

static unsigned short get_tcp_check_sum(unsigned int sourceAddress, unsigned int targetAddress, unsigned short tcp_total_length, unsigned char* tcp_binary){
    char* checkData;

    int size = 12 + tcp_total_length;

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
    checkData[9] = 0x06;

    checkData[10] = (unsigned char)(tcp_total_length >> 8);
    checkData[11] = (unsigned char)(tcp_total_length & 0x00FF);

    for (int i = 0; i < tcp_total_length; i++){
        checkData[12 + i] = tcp_binary[i];
    }

    unsigned short cs = tcp_check_sum((unsigned const short*) checkData, size);

    free(checkData);

    return cs;
}

#endif