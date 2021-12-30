package com.lhr.vpn.proxy.state

import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.proxy.TCPProxyClient
import kotlin.random.Random

/**
 * @author lhr
 * @date 2021/12/29
 * @des 关闭状态
 */
class TCPClosedAction(private val client: TCPProxyClient): SocketAction {
    override fun receive(packet: IPPacket) {
        Log.e("Test","receive tcp is closed")
    }

    override fun send(packet: IPPacket) {
        Log.e("Test","send tcp is closed")
    }

    private fun generateRst(): TCPPacket {
        val ackPacket = TCPPacket()
        ackPacket.setTargetPort(client.proxyConfig.sourcePort)
        ackPacket.setTargetAddress(client.proxyConfig.sourceAddress)
        ackPacket.setSourcePort(client.proxyConfig.targetPort)
        ackPacket.setSourceAddress(client.proxyConfig.targetAddress)
        ackPacket.setOffsetFrag(0)
        ackPacket.setFlag(2)
        ackPacket.setTimeToLive(64)
        ackPacket.setIdentification(Random(System.currentTimeMillis()).nextInt().toShort())
        ackPacket.setControlFlag(TCPPacket.ControlFlag.RST)
        return ackPacket
    }
}