package com.lhr.vpn.protocol

/**
 * @author lhr
 * @date 2021/11/11
 * @des tcp 数据报
 */
class TCPPacket: IPacket{
    override fun getType(): IPacket.PacketType = IPacket.PacketType.TCP
    override fun getHeader(): IPacket.IPacketHeader {
        return object : IPacket.IPacketHeader{}
    }
}