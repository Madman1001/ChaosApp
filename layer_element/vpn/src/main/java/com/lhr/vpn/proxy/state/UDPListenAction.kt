package com.lhr.vpn.proxy.state

import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.proxy.UDPProxyClient

/**
 * @author lhr
 * @date 2021/12/30
 * @des udp等待响应动作, 这里不进行出来，直接让客户端转发即可
 */
class UDPListenAction(private val client: UDPProxyClient): SocketAction {
    override fun receive(packet: IPPacket) {
        client.externalToInternal(packet)
    }

    override fun send(packet: IPPacket) {
        client.internalToExternal(packet)
    }
}