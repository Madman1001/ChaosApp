package com.lhr.vpn.socks.handle

import com.lhr.vpn.socks.net.v4.NetUdpPacket

/**
 * @author lhr
 * @date 13/10/2022
 * @des udp packet handler
 */
class UdpPacketHandle: INetHandle<NetUdpPacket> {
    //udp数据处理列表
    private val handleList by lazy { mutableListOf<INetHandle<NetUdpPacket>>() }

    override fun onHandleOutPacket(packet: NetUdpPacket): Boolean {
        return true
    }

    override fun onHandleInPacket(packet: NetUdpPacket): Boolean {
        return true
    }
}