package com.lhr.vpn.socks.handle

import com.lhr.vpn.socks.net.v4.NetTcpHeader

/**
 * @author lhr
 * @date 13/10/2022
 * @des tcp packet handler
 */
class TcpPacketHandle: INetHandle<NetTcpHeader> {
    //tcp数据处理列表
    private val handleList by lazy { mutableListOf<INetHandle<NetTcpHeader>>() }

    override fun onHandleOutPacket(packet: NetTcpHeader): Boolean {
        return true
    }

    override fun onHandleInPacket(packet: NetTcpHeader): Boolean {
        return true
    }
}