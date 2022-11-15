package com.lhr.vpn.socks.net

/**
 * @author lhr
 * @date 23/10/2022
 * @des 协议相关常量
 */

const val IP_VERSION_4 = 4 //0100
const val IP_VERSION_6 = 6 //0110

const val PROTO_TCP = 6
const val PROTO_UDP = 17
const val PROTO_ICMP = 1
const val PROTO_IGMP = 2

const val MAX_PACKET_SIZE = 1799
const val MAX_IP_PACKET_HEADER_SIZE = 0xF

/**
 * Tcp control sign
 */
//(0 0 URG ACK PSH RST SYN FIN)
const val SIGN_NUL = 0x00
const val SIGN_URG = 0x20
const val SIGN_ACK = 0x10
const val SIGN_PSH = 0x08
const val SIGN_RST = 0x04
const val SIGN_SYN = 0x02
const val SIGN_FIN = 0x01