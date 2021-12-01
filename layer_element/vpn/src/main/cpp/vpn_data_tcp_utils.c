#ifndef _Included_vpn_data_tcp_utils
#define _Included_vpn_data_tcp_utils

/**
 * tcp 协议读取工具
 */

/**
* 读取tcp源端口号
*/
static unsigned short tcp_read_source_port(const char *data) {
    unsigned short source_port = (unsigned short) data[0];
    source_port = (source_port << 8) | (unsigned short) data[1];
    return source_port;
}

/**
* 读取tcp目标端口号
*/
static unsigned short tcp_read_target_port(const char *data) {
    unsigned short target_port = (unsigned short) data[2];
    target_port = (target_port << 8) | (unsigned short) data[3];
    return target_port;
}

/**
* 读取tcp序号
*/
static unsigned int tcp_read_serial_number(const char *data) {
    unsigned int serial_number = ((int *) data)[1];
    return serial_number;
}

/**
* 读取tcp确认序号
*/
static unsigned int tcp_read_verify_serial_number(const char *data) {
    unsigned int verify_serial_number = ((int *) data)[2];
    return verify_serial_number;
}

/**
* 读取tcp首部长度
*/
static unsigned char tcp_read_head_length(const char *data) {
    unsigned char head_length = data[12];
    return head_length >> 4;
}

/**
 * 读取tcp控制位
 */
static unsigned char tcp_read_control_sign(const char *data) {
    unsigned char control_sign = data[13];
    return control_sign;
}

/**
 * 读取tcp窗口大小
 */
static unsigned short tcp_read_window_size(const char *data) {
    unsigned short window_size = ((short *)data)[7];
    return window_size;
}

/**
 * 读取tcp校验和
 */
static unsigned short tcp_read_check_sum(const char *data) {
    unsigned short check_sum = ((short *)data)[8];
    return check_sum;
}

/**
 * 读取tcp紧急指针
 */
static unsigned short tcp_read_urgent_pointer(const char *data) {
    unsigned short urgent_pointer = ((short *)data)[9];
    return urgent_pointer;
}

/**
* 读取tcp其它选项
*/
static void tcp_read_head_other_option(const char *data, char *head_other_option, int offset, int length) {
    int i = 0;
    for (; i < length; ++i) {
        head_other_option[i] = data[offset + i];
    }
}

/**
* 读取tcp数据
*/
static void tcp_read_data(const char *data, char *tcp_data, int offset, int length) {
    int i = 0;
    for (; i < length; ++i) {
        tcp_data[i] = data[offset + i];
    }
}

#endif