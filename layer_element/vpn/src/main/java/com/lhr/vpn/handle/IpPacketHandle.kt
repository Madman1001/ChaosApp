package com.lhr.vpn.handle

import com.lhr.vpn.net.v4.NetIpPacket

/**
 * @author lhr
 * @date 13/10/2022
 * @des ip packet handler
 */
class IpPacketHandle: INetHandle<NetIpPacket> {
    override fun onHandle(packet: NetIpPacket): Boolean {
        return true
    }
}