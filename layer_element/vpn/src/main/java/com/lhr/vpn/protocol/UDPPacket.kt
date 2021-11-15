package com.lhr.vpn.protocol

/**
 * @author lhr
 * @date 2021/11/11
 * @des udp数据报
 */
class UDPPacket: IPacket {
    override fun getType(): IPacket.PacketType = IPacket.PacketType.UDP
}