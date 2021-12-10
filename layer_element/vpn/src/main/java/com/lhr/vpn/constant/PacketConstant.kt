package com.lhr.vpn.constant

/**
 * @author lhr
 * @date 2021/11/11
 * @des 数据报
 */
object PacketConstant {

    /**
     * 不同协议的数据报
     */
    enum class DataType(val value: Int) {
        TCP(6),
        UDP(17),
        ICMP(1),
        IGMP(2),
    }

    enum class DataOperateType(val value: Int){
        IP_VERSION(1),
        IP_UPPER_PROTOCOL(2),
        IP_DATA(3),
        IP_SOURCE_ADDRESS(4),
        IP_TARGET_ADDRESS(5),

        UDP_SOURCE_PORT(6),
        UDP_TARGET_PORT(7),
        UDP_DATA(8),
        TCP_SOURCE_PORT(9),
        TCP_TARGET_PORT(10),

        IP_FLAG(11),
        IP_OFFSET_FRAG(12),
        IP_TIME_TO_LIVE (13),
        IP_IDENTIFICATION (14),
    }
}