package com.lhr.vpn.proxy.state

import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.proxy.TCPProxyClient
import kotlin.random.Random

/**
 * @author lhr
 * @date 2021/12/29
 * @des 监听状态，等待客户端连接
 */
class TCPListenAction(private val client: TCPProxyClient): SocketAction {
    override fun receive(packet: IPPacket) {

    }

    override fun send(packet: IPPacket) {
        if (packet is TCPPacket){
            if (packet.isControlFlag(TCPPacket.ControlFlag.SYN)){
                if (client.connect()) {
                    val synAck = generateSynAckPacket(packet)
                    client.externalToInternal(synAck)
                    //客户端进入下一阶段
                    client.action = TCPSynRcvdAction(client)
                }
            } else {
            }
        }
    }

    /**
     * 生成客户端进行握手ip数据报
     */
    private fun generateSynAckPacket(tcpPacket: TCPPacket): IPPacket{
        client.serverSerialNumber = Random.nextInt().toLong()
        client.clientSerialNumber = tcpPacket.getSerialNumber() + 1
        val packet = TCPPacket()
        packet.setTargetPort(client.proxyConfig.sourcePort)
        packet.setTargetAddress(client.proxyConfig.sourceAddress)
        packet.setSourcePort(client.proxyConfig.targetPort)
        packet.setSourceAddress(client.proxyConfig.targetAddress)
        packet.setOffsetFrag(0)
        packet.setFlag(2)
        packet.setTimeToLive(64)
        packet.setIdentification(Random(System.currentTimeMillis()).nextInt().toShort())
        packet.setSerialNumber(client.serverSerialNumber)
        packet.setVerifySerialNumber(client.clientSerialNumber)
        packet.setControlFlag(TCPPacket.ControlFlag.ACK)
        packet.setControlFlag(TCPPacket.ControlFlag.SYN)

        val packetMss = tcpPacket.getMSS()
        if (packetMss != 0){
            packet.setMSS(packetMss)
        }

        val packetWsopt = tcpPacket.getWSOPT()
        if (packetWsopt != 0){
            packet.setWSOPT(packetWsopt.toByte())
        }

        if (tcpPacket.getSACK_P()){
            packet.setSACK_P(true)
        }

//        val packetTsopt = tcpPacket.getTSOPT()
//        if (packetTsopt[0] != 0L){
//            packet.setTSOPT(packetTsopt[0])
//        }
        return packet
    }
}