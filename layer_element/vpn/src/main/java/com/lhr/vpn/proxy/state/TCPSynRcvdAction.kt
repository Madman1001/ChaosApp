package com.lhr.vpn.proxy.state

import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.proxy.TCPProxyClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author lhr
 * @date 2021/12/29
 * @des 收到了对方建立连接的请求且发送了建立连接的请求（确认对方建立建立连接的请求）。等待对方确认自己发送的连接请求
 */
class TCPSynRcvdAction(private val client: TCPProxyClient): SocketAction {
    private val timeoutListener: Job
    init {
        //启动监听器，如果3秒后未连接完成，则直接关闭连接
        timeoutListener = GlobalScope.launch {
            delay(3000L)
            if (client.action == this){
                client.stop()
            }
        }
    }

    override fun receive(packet: IPPacket) {

    }

    override fun send(packet: IPPacket) {
        if (packet is TCPPacket){
            if (packet.isControlFlag(TCPPacket.ControlFlag.ACK)
                && packet.getVerifySerialNumber() == client.serverSerialNumber + 1){
                timeoutListener.cancel()
                //完成确认，进入下一阶段

                Log.e("Test","Ack Packet, enter TCPEstablishedAction:${client.clientSerialNumber}")
                client.action = TCPEstablishedAction(client)
            }
        }
    }
}