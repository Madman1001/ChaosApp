#ifndef _Included_vpn_data_udp_utils
#define _Included_vpn_data_udp_utils

/**
 * udp 协议读取工具
 */

/**
* 读取udp源端口号
*/
static unsigned short udp_read_source_port(const char *data) {
    unsigned short source_port = (unsigned short)data[0];
    source_port = (source_port << 8) | (unsigned short)data[1];
    return source_port;
}

/**
* 读取udp目标端口号
*/
static unsigned short udp_read_target_port(const char *data) {
    unsigned short target_port = (unsigned short)data[2];
    target_port = (target_port << 8) | (unsigned short)data[3];
    return target_port;
}

/**
* 读取udp长度
*/
static unsigned short udp_read_length(const char *data) {
    unsigned short length = (unsigned short)data[4];
    length = (length << 8) | (unsigned short)data[5];
    return length;
}

/**
* 读取udp校验和
*/
static unsigned short udp_read_check_sum(const char *data) {
    unsigned short check_sum = (unsigned short)data[6];
    check_sum = (check_sum << 8) | (unsigned short)data[7];
    return check_sum;
}

/**
* 读取udp数据
*/
static void udp_read_data(const char *data, char *udp_data, int offset, int length){
    int i = 0;
    for (; i < length; ++i) {
        udp_data[i] = data[offset + i];
    }
}

#endif