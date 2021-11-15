package com.lhr.vpn.protocol

/**
 * @author lhr
 * @date 2021/11/11
 * @des 数据报
 */
interface IPacket {
    /**
     * 不同协议的数据报
     */
    enum class PacketType {
        TCP, UDP, ICMP, IGMP
    }

    /**
     * 数据报类型
     */
    fun getType(): PacketType

    /**
     * 获取数据报报文头
     */
    fun getHeader(): IPacketHeader

    interface IPacketHeader
}