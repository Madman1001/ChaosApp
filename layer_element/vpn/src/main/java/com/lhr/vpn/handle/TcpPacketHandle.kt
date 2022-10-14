package com.lhr.vpn.handle

import com.lhr.vpn.socks.net.v4.NetTcpPacket

/**
 * @author lhr
 * @date 13/10/2022
 * @des tcp packet handler
 */
class TcpPacketHandle: INetHandle<NetTcpPacket> {
    override fun onHandle(packet: NetTcpPacket): Boolean {
        return true
    }
}