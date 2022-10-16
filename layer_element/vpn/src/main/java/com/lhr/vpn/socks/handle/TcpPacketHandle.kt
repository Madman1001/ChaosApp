package com.lhr.vpn.socks.handle

import com.lhr.vpn.socks.net.v4.NetTcpPacket

/**
 * @author lhr
 * @date 13/10/2022
 * @des tcp packet handler
 */
class TcpPacketHandle: INetHandle<NetTcpPacket> {
    //tcp数据处理列表
    private val handleList by lazy { mutableListOf<INetHandle<NetTcpPacket>>() }

    override fun onHandleOutPacket(packet: NetTcpPacket): Boolean {
        return true
    }

    override fun onHandleInPacket(packet: NetTcpPacket): Boolean {
        return true
    }
}