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

    /**
     * 数据操作类型，数值由native层确定
     */
    enum class DataOperateType(val value: Int){
        IP_VERSION(1),
        IP_UPPER_PROTOCOL(2),
        IP_DATA(3),
        IP_SOURCE_ADDRESS(4),
        IP_TARGET_ADDRESS(5),
        IP_FLAG(6),
        IP_OFFSET_FRAG(7),
        IP_TIME_TO_LIVE (8),
        IP_IDENTIFICATION (9),

        UDP_SOURCE_PORT(10),
        UDP_TARGET_PORT(11),
        UDP_DATA(12),

        TCP_SOURCE_PORT(13),
        TCP_TARGET_PORT(14),
        TCP_SERIAL_NUMBER(15),
        TCP_VERIFY_SERIAL_NUMBER(16),
        TCP_CONTROL_SIGN(17),
        TCP_WINDOW_SIZE(18),
        TCP_URGENT_POINTER(19),
        TCP_OPTIONS(20),
        TCP_DATA(21)

    }
}