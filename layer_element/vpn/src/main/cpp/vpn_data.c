#ifndef _Included_vpn_data
#define _Included_vpn_data

#include "vpn_data_ip_utils.c"
#include "vpn_data_tcp_utils.c"
#include "vpn_data_udp_utils.c"

//网际控制报文协议
#define PACKET_TYPE_ICPM 1
//网际组管理协议
#define PACKET_TYPE_IGMP 2
//网关——网关协议
#define PACKET_TYPE_GGP 3
//流
#define PACKET_TYPE_ST 5
//传输控制协议
#define PACKET_TYPE_TCP 6
//外部网关协议
#define PACKET_TYPE_EGP 8
//内部网关协议
#define PACKET_TYPE_IGP 9
//网络声音协议
#define PACKET_TYPE_NVP 11
//用户数据报协议
#define PACKET_TYPE_UDP 17

#define IP_VERSION_V4 4
#define IP_VERSION_V6 6

struct IP_Packet {
    //版本号 4 bit
    unsigned char version;

    //头长度 4 bit (单位 32 bit)
    unsigned char head_length;

    //服务类型 8 bit
    unsigned char type_of_service;

    //总长度 16 bit (单位 8 bit)
    unsigned short total_length;

    //标识 16 bit
    unsigned short identification;

    //标志 3 bit
    unsigned char flag;

    //片偏移 13 bit (记录分片在原报文中的相对位置，以8个字节为偏移单位)
    unsigned short offset_frag;

    //生存时间 8 bit
    unsigned char time_to_live;

    //上层协议 8 bit
    unsigned char upper_protocol;

    //头部校验和 16 bit
    unsigned short head_check_sum;

    //源ip地址 32 bit
    unsigned int source_ip_address;

    //目标ip地址 32 bit
    unsigned int target_ip_address;

    //可选字段 (单位 32 bit)
    void* head_other_data;

    //数据
    void* data;
};

struct UDP_Packet {
    //源端口号 16 bit
    unsigned short source_port;

    //目标端口号 16 bit
    unsigned short target_port;

    //UDP长度(单位为：字节) 16 bit
    unsigned short total_length;

    //UDP校验和 16 bit
    unsigned short check_sum;

    //UDP数据块长度(单位：字节)
    unsigned short data_length;

    //数据
    void* data;
};

struct TCP_Packet {
    //源端口号 16 bit
    unsigned short source_port;

    //目标端口号 16 bit
    unsigned short target_port;

    //序号 32 bit
    unsigned int serial_number;

    //确认序号 32 bit
    unsigned int verify_serial_number;

    //首部长度 4 bit (以 4 字节为单位)
    unsigned char head_length;

    //UDP长度(单位为：字节) 16 bit
    unsigned int total_length;

    //保留位 6 bit
    unsigned char keep_position;

    //控制位 6 bit (0 0 URG ACK PSH RST SYN FIN)
    unsigned char control_sign;

    //窗口大小 16 bit
    unsigned short window_size;

    //校验和 16 bit
    unsigned short check_sum;

    //紧急指针 16 bit
    unsigned short urgent_pointer;

    //其它选项
    void* head_other_data;

    //数据
    void* data;
};

#endif