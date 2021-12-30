package com.lhr.vpn.proxy.state

import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.proxy.TCPProxyClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * @author lhr
 * @date 2021/12/29
 * @des 确认了对方的关闭连接请求，等待本地用户关闭连接指令
 */
class TCPCloseWaitAction(private val client: TCPProxyClient): SocketAction {
    private val timeoutListener: Job
    init {
        //启动监听器，如果20秒后未完成，则直接关闭连接
        timeoutListener = GlobalScope.launch {
            delay(5000L)
            if (client.action == this){
                client.stop()
            }
        }
        val ackFin = generateFin()
        ackFin.setSerialNumber(client.serverSerialNumber)
        ackFin.setVerifySerialNumber(client.clientSerialNumber)
        client.externalToInternal(ackFin)
    }

    override fun receive(packet: IPPacket) {

    }

    override fun send(packet: IPPacket) {
        if (packet is TCPPacket){
            if (packet.getVerifySerialNumber() == client.serverSerialNumber + 1){
                timeoutListener.cancel()
                //完成确认，进入下一阶段
                client.stop()
                client.action = TCPClosedAction(client)
                Log.e("Test","Ack Packet, enter TCPClosedAction:${client.clientSerialNumber}")
            }
        }
    }

    private fun generateFin(): TCPPacket {
        val ackPacket = TCPPacket()
        ackPacket.setTargetPort(client.proxyConfig.sourcePort)
        ackPacket.setTargetAddress(client.proxyConfig.sourceAddress)
        ackPacket.setSourcePort(client.proxyConfig.targetPort)
        ackPacket.setSourceAddress(client.proxyConfig.targetAddress)
        ackPacket.setOffsetFrag(0)
        ackPacket.setFlag(2)
        ackPacket.setTimeToLive(64)
        ackPacket.setIdentification(Random(System.currentTimeMillis()).nextInt().toShort())
        ackPacket.setControlFlag(TCPPacket.ControlFlag.FIN)
        ackPacket.setControlFlag(TCPPacket.ControlFlag.ACK)
        return ackPacket
    }
}