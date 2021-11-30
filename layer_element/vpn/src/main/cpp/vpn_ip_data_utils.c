#ifndef _Included_vpn_ip_data_utils
#define _Included_vpn_ip_data_utils

/**
 * ip 协议读取工具
 */

/**
 * 读取ip数据报版本号
 */
static unsigned char ip_read_version(const char *data) {
    unsigned char version = data[0];
    return version >> 4;
}

/**
 * 读取ip数据报头部长度
 */
static unsigned char ip_read_head_length(const char *data) {
    unsigned char headLen = data[0];
    return headLen & 0x0F;
}

/**
 * 读取ip服务类型TOS
 */
 static unsigned char ip_read_tos(const char *data){
    return (unsigned char)data[1];
 }

/**
* 读取ip总长度
*/
static unsigned short ip_read_total_length(const char *data){
    unsigned short totlen = (unsigned short) data[2];

    totlen = totlen << (unsigned short ) 8;
    totlen |= (unsigned short)data[3];
    return totlen;
}

/**
* 读取ip标识
*/
static unsigned short ip_read_identification(const char *data){
    unsigned short identification = (unsigned short) data[4];

    identification = identification << (unsigned short )8;
    identification |= (unsigned short) data[5];
    return identification;
}

/**
* 读取ip标志
*/
static unsigned char ip_read_flag(const char *data){
    unsigned char sign = (unsigned char)data[6];
    sign = sign >> 5;
    return sign;
}

/**
* 读取ip片偏移
*/
static unsigned short ip_read_offset_frag(const char *data){
    unsigned short offsetFrag = (unsigned short) data[6];
    offsetFrag = offsetFrag << 8;
    offsetFrag &= 0x1FFF;
    offsetFrag |= (unsigned short) data[7];
    return offsetFrag;
}

/**
* 读取ip生存时间
*/
static unsigned char ip_read_ttl(const char *data){
    unsigned char ttl = (unsigned short) data[8];
    return ttl;
}

/**
* 读取ip上层协议
*/
static unsigned char ip_read_upper_protocol(const char *data){
    unsigned char up = (unsigned short) data[9];
    return up;
}

/**
* 读取ip头部校验和
*/
static unsigned char ip_read_head_check_sum(const char *data){
    unsigned short hcs = (unsigned short) data[10];
    hcs = hcs << 8;
    hcs |= (unsigned short)data[11];
    return hcs;
}

/**
* 读取ip源ip地址
*/
static void ip_read_source_ip_address(const char *data, unsigned char *sourceIp) {
    int i = 0;
    for (; i < 4; ++i) {
        sourceIp[i] = data[12 + i];
    }
}

/**
* 读取ip目标ip地址
*/
static void ip_read_target_ip_address(const char *data, unsigned char *targetIp){
    int i = 0;
    for (; i < 4; ++i) {
        targetIp[i] = data[16 + i];
    }
}

/**
* 读取ip其它选项
*/
static void ip_read_other_head_fields(const char *data, char *other_head_fields, int offset, int length){
    int i = 0;
    for (; i < length; ++i) {
        other_head_fields[i] = data[offset + i];
    }
}

/**
* 读取ip数据
*/
static void ip_read_data(const char *data, char *ip_data, int offset, int length){
    int i = 0;
    for (; i < length; ++i) {
        ip_data[i] = data[offset + i];
    }
}
#endif