package com.lhr.vpn.socks.handle

import com.lhr.vpn.socks.net.v4.NetUdpHeader

/**
 * @author lhr
 * @date 13/10/2022
 * @des udp packet handler
 */
class UdpPacketHandle: INetHandle<NetUdpHeader> {
    //udp数据处理列表
    private val handleList by lazy { mutableListOf<INetHandle<NetUdpHeader>>() }

    override fun onHandleOutPacket(packet: NetUdpHeader): Boolean {
        return true
    }

    override fun onHandleInPacket(packet: NetUdpHeader): Boolean {
        return true
    }
}