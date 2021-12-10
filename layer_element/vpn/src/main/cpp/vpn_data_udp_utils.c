#ifndef _Included_vpn_data_udp_utils
#define _Included_vpn_data_udp_utils

/**
 * udp 协议读取工具
 */

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

#endif