#ifndef _Included_vpn_data_ip_utils
#define _Included_vpn_data_ip_utils

/**
 * ip 协议读取工具
 */

/**
 * 读取ip数据报版本号
 */
static unsigned char ip_read_version(unsigned const char *data) {
    return (unsigned char)data[0] >> 4;
}

/**
 * 写入ip数据报版本号
 */
static void ip_write_version(unsigned char *data, unsigned char version) {
    data[0] = ((unsigned char)data[0] & (unsigned char)0x0F) | ((unsigned char)version << (unsigned char)4);
}


/**
 * 读取ip数据报头部长度
 */
static unsigned char ip_read_head_length(unsigned const char *data) {
    return (unsigned char)data[0] & 0x0F;
}

/**
 * 写入ip数据报头部长度
 */
static void ip_write_head_length(unsigned char *data, unsigned char head_length) {
    data[0] = ((unsigned char)data[0] & (unsigned char)0xF0) | head_length;
}

/**
 * 读取ip服务类型TOS
 */
static unsigned char ip_read_tos(unsigned const char *data){
    return (unsigned char)data[1];
}

/**
 * 写入ip服务类型TOS
 */
static void ip_write_tos(unsigned char *data, unsigned char tos){
    data[1] = tos;
}

/**
* 读取ip总长度
*/
static unsigned short ip_read_total_length(unsigned const char *data){
    unsigned short totlen = (unsigned short) data[2];
    totlen = totlen << (unsigned short ) 8;
    totlen |= (unsigned short)data[3];
    return totlen;
}

/**
* 写入ip总长度
*/
static void ip_write_total_length(unsigned char *data, unsigned short total_length){
    data[2] = total_length >> 8;
    data[3] = total_length & 0x00FF;
}

/**
* 读取ip标识
*/
static unsigned short ip_read_identification(unsigned const char *data){
    unsigned short identification = (unsigned char) data[4];

    identification = identification << (unsigned char)8;
    identification |= (unsigned char) data[5];
    return identification;
}

/**
* 写入ip标识
*/
static void ip_write_identification(unsigned char *data, unsigned short identification){
    data[4] = identification >> 8;
    data[5] = identification & 0x00FF;
}

/**
* 读取ip标志
*/
static unsigned char ip_read_flag(unsigned const char *data){
    unsigned char sign = (unsigned char)data[6];
    sign = sign >> 5;
    return sign;
}

/**
* 写入ip标志
*/
static void ip_write_flag(unsigned char *data, unsigned char flag){
    data[6] = ((unsigned char)data[6] & (unsigned char) 0x1F )| (flag << (unsigned char)5);
}

/**
* 读取ip片偏移
*/
static unsigned short ip_read_offset_frag(unsigned const char *data){
    unsigned short offsetFrag = (unsigned char) data[6];
    offsetFrag = offsetFrag << 8;
    offsetFrag &= 0x1FFF;
    offsetFrag |= (unsigned char) data[7];
    return offsetFrag;
}

/**
* 写入ip片偏移
*/
static void ip_write_offset_frag(unsigned char *data, unsigned short offset_frag){
    data[6] = (data[6] & 0xE0) | ((unsigned char)(offset_frag >> 8) & (unsigned char)0x1F);
    data[7] = (unsigned char)(offset_frag & (unsigned short)0x00FF);
}

/**
* 读取ip生存时间
*/
static unsigned char ip_read_ttl(unsigned const char *data){
    unsigned char ttl = (unsigned char) data[8];
    return ttl;
}

/**
* 写入ip生存时间
*/
static void ip_write_ttl(unsigned char *data, unsigned char ttl){
    data[8] = ttl;
}

/**
* 读取ip上层协议
*/
static unsigned char ip_read_upper_protocol(unsigned const char *data){
    unsigned char up = (unsigned char) data[9];
    return up;
}

/**
* 写入ip上层协议
*/
static void ip_write_upper_protocol(unsigned char *data, unsigned char up){
    data[9] = up;
}

/**
* 读取ip头部校验和
*/
static unsigned short ip_read_head_check_sum(unsigned const char *data){
    return ((unsigned short*) data)[5];
}

/**
* 写入ip头部校验和
*/
static void ip_write_head_check_sum(unsigned char *data, unsigned short hcs){
    ((unsigned short*) data)[5] = hcs;
}

/**
* 读取ip源ip地址
*/
static unsigned int ip_read_source_ip_address(unsigned const char *data) {
    unsigned char * uData = (unsigned char *)data;
    unsigned int address = 0;
    for (int i = 0; i < 4; ++i) {
        address = address << (unsigned int)8;
        address |= (unsigned int)uData[12 + i];
    }
    return address;
}

/**
* 写入ip源ip地址
*/
static void ip_write_source_ip_address(unsigned char *data, unsigned int address) {
    data[12] = (unsigned char)((address >> 24) & 0x000000FF);
    data[13] = (unsigned char)((address >> 16) & 0x000000FF);
    data[14] = (unsigned char)((address >> 8) & 0x000000FF);
    data[15] = (unsigned char)(address & 0x000000FF);
}

/**
* 读取ip目标ip地址
*/
static unsigned int ip_read_target_ip_address(unsigned const char *data){
    unsigned char * uData = (unsigned char *)data;
    unsigned int address = 0;
    for (int i = 0; i < 4; ++i) {
        address = address << (unsigned int)8;
        address |= (unsigned int)uData[16 + i];
    }
    return address;
}

/**
* 写入ip目标ip地址
*/
static void ip_write_target_ip_address(unsigned char *data, unsigned int address) {
    data[16] = (unsigned char)((address >> 24) & 0x000000FF);
    data[17] = (unsigned char)((address >> 16) & 0x000000FF);
    data[18] = (unsigned char)((address >> 8) & 0x000000FF);
    data[19] = (unsigned char)(address & 0x000000FF);
}

/**
* 读取ip其它选项
*/
static void ip_read_other_head_fields(unsigned const char *data,unsigned char *other_head_fields, int offset, int length){
    int i = 0;
    for (; i < length; ++i) {
        other_head_fields[i] = data[offset + i];
    }
}

/**
* 写入ip目标ip地址
*/
static void ip_write_other_head_fields(unsigned char *data, unsigned const char *other_head_fields, int offset, int length) {
    int i = 0;
    for (; i < length; ++i) {
        data[offset + i] = other_head_fields[i];
    }
}

/**
* 读取ip数据
*/
static void ip_read_data(unsigned const char *data, unsigned char *ip_data, int offset, int length){
    int i = 0;
    for (; i < length; ++i) {
        ip_data[i] = data[offset + i];
    }
}

/**
* 写入ip数据
*/
static void ip_write_data(unsigned char *data,unsigned const char *ip_data, int offset, int length) {
    int i = 0;
    for (; i < length; ++i) {
        data[offset + i] = ip_data[i];
    }
}
#endif