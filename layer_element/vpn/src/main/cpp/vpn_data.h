#ifndef _Included_vpn_data_h
#define _Included_vpn_data_h

struct IP_Packet{
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
    unsigned char* source_ip_address;

    //目标ip地址 32 bit
    unsigned char* target_ip_address;

    //可选字段 (单位 32 bit)
    char* other_head_fields;

    //数据
    char* data;
};

#endif