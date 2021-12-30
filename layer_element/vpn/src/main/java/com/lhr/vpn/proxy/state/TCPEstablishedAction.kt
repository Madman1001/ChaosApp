package com.lhr.vpn.proxy.state

import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.proxy.TCPProxyClient
import kotlin.random.Random

/**
 * @author lhr
 * @date 2021/12/29
 * @des 连接已经建立，可以正常进行数据传输
 */
class TCPEstablishedAction(private val client: TCPProxyClient) : SocketAction {
    override fun receive(packet: IPPacket) {
        if (packet is TCPPacket){
            packet.setSerialNumber(client.serverSerialNumber)
            packet.setVerifySerialNumber(client.clientSerialNumber)
            packet.setControlFlag(TCPPacket.ControlFlag.ACK)
            packet.setControlFlag(TCPPacket.ControlFlag.PSH)
            client.externalToInternal(packet)
        }
    }

    override fun send(packet: IPPacket) {
        if (packet is TCPPacket) {
            //关闭连接
            if (packet.isControlFlag(TCPPacket.ControlFlag.FIN)
                && packet.isControlFlag(TCPPacket.ControlFlag.ACK)
                && packet.getVerifySerialNumber() == client.serverSerialNumber){
                client.serverSerialNumber = packet.getVerifySerialNumber()
                client.clientSerialNumber = packet.getSerialNumber() + 1
                val ack = generateAck()
                ack.setSerialNumber(client.serverSerialNumber)
                ack.setVerifySerialNumber(client.clientSerialNumber)
                //等待连接关闭
                client.action = TCPCloseWaitAction(client)
                client.externalToInternal(ack)
                Log.e("Test","Ack Packet, enter TCPCloseWaitAction:${client.clientSerialNumber}")
            }else if (packet.isControlFlag(TCPPacket.ControlFlag.ACK)
                && packet.getVerifySerialNumber() == client.serverSerialNumber + 1
            ) {
                //请求数据
                val dataLength = packet.getData().size
                if (dataLength > 0){
                    client.internalToExternal(packet)
                }
                client.serverSerialNumber = packet.getVerifySerialNumber()
                client.clientSerialNumber = packet.getSerialNumber() + dataLength
                val ack = generateAck()
                ack.setSerialNumber(client.serverSerialNumber)
                ack.setVerifySerialNumber(client.clientSerialNumber)
                client.externalToInternal(ack)
                Log.e("Test", "${client.serverSerialNumber} : ${client.clientSerialNumber}")
            }
        }
    }

    private fun generateAck(): TCPPacket{
        val ackPacket = TCPPacket()
        ackPacket.setTargetPort(client.proxyConfig.sourcePort)
        ackPacket.setTargetAddress(client.proxyConfig.sourceAddress)
        ackPacket.setSourcePort(client.proxyConfig.targetPort)
        ackPacket.setSourceAddress(client.proxyConfig.targetAddress)
        ackPacket.setOffsetFrag(0)
        ackPacket.setFlag(2)
        ackPacket.setTimeToLive(64)
        ackPacket.setIdentification(Random(System.currentTimeMillis()).nextInt().toShort())
        ackPacket.setControlFlag(TCPPacket.ControlFlag.ACK)
        return ackPacket
    }
}