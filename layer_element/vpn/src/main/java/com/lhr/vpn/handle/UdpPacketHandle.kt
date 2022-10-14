package com.lhr.vpn.handle

import com.lhr.vpn.socks.net.v4.NetUdpPacket

/**
 * @author lhr
 * @date 13/10/2022
 * @des udp packet handler
 */
class UdpPacketHandle: INetHandle<NetUdpPacket> {
    override fun onHandle(packet: NetUdpPacket): Boolean {
        return true
    }
}