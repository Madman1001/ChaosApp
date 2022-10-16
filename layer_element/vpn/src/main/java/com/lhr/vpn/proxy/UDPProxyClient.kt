package com.lhr.vpn.proxy

import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.UDPPacket
import com.lhr.vpn.proxy.state.SocketAction
import com.lhr.vpn.proxy.state.UDPListenAction
import com.lhr.vpn.proxy.state.UselessAction
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*
import kotlin.random.Random

/**
 * @author lhr
 * @date 2021/12/4
 * @des udp 客户端代理类
 */
class UDPProxyClient(
    private val handleTun: IProxyTun,
    private val udpSocket: DatagramSocket,
    private val proxyConfig: ProxyConfig
) : IProxyClient {
    private val tag = "UDPProxyClient"
    private val packetList = Vector<UDPPacket>()
    private var currentUdpSendThread: Thread? = null
    private var currentUdpReceiveThread: Thread? = null

    private var action: SocketAction = UselessAction(tag)
    /**
     * 连接的最大等待时长
     */
    private val liveTime: Long = 5000

    @Volatile
    private var isStart = false

    @Synchronized
    fun start(){
        if (!isStart) {
            action = UDPListenAction(this)
            isStart = true
            Thread({
                sendRun()
            }, tag).start()
            Thread({
                receiveRun()
            }, tag).start()
        }
    }

    @Synchronized
    fun stop() {
        if (isStart){
            isStart = false
            action = UselessAction(tag)
            if (currentUdpSendThread != null) {
                if (currentUdpSendThread?.state == Thread.State.TIMED_WAITING) {
                    currentUdpSendThread?.interrupt()
                }
            }

            if (currentUdpReceiveThread != null){
                if (currentUdpReceiveThread?.state == Thread.State.TIMED_WAITING) {
                    currentUdpReceiveThread?.interrupt()
                }
            }
        }
    }

    fun pushPacket(packet: UDPPacket) {
        if (!isStart){
            start()
        }

        packetList.add(packet)
        if (currentUdpSendThread?.state == Thread.State.TIMED_WAITING) {
            currentUdpSendThread?.interrupt()
        }
    }

    override fun internalToExternal(packet: IPPacket) {
        val buf = packet.getData()
        val address = InetSocketAddress(packet.getTargetAddress(), packet.getTargetPort())
        val udpPacket = DatagramPacket(buf, buf.size, address)
        udpSocket.send(udpPacket)
    }

    override fun externalToInternal(packet: IPPacket) {
        handleTun.outputData(packet)
    }

    private fun sendRun() {
        currentUdpSendThread = Thread.currentThread()
        while (isStart) {
            if (packetList.isNotEmpty()) {
                Log.d(tag, "start proxy udp send")
                val packet = packetList.removeFirst()
                if (packet.getSourcePort() == udpSocket.localPort) {
                    throw RuntimeException("udp socket 出现环路")
                }
                action.send(packet)
                Log.d(tag, "end proxy udp send")
            } else {
                try {
                    Thread.sleep(liveTime)
                    stop()
                    break
                } catch (e: InterruptedException) {
                    Log.e(tag, "线程被唤醒")
                }
            }
        }
    }

    private fun receiveRun() {
        currentUdpReceiveThread = Thread.currentThread()
        val data = ByteArray(1024)
        val receivePacket = DatagramPacket(data, data.size)
        Log.d(tag, "start proxy udp receive")
        while (isStart) {
            try {
                udpSocket.receive(receivePacket)
                val str = String(receivePacket.data, 0, receivePacket.length)
                Log.d(tag, "proxy udp receive:$str")
                val packet = UDPPacket().apply {
                    this.setData(receivePacket.data, receivePacket.offset, receivePacket.length)
                    this.setTargetPort(proxyConfig.sourcePort)
                    this.setTargetAddress(proxyConfig.sourceAddress)
                    this.setSourcePort(proxyConfig.targetPort)
                    this.setSourceAddress(proxyConfig.targetAddress)
                    this.setOffsetFrag(0)
                    this.setFlag(2)
                    this.setTimeToLive(64)
                    this.setIdentification(Random(System.currentTimeMillis()).nextInt().toShort())
                }
                action.receive(packet)
            } catch (e: Exception) {
            }
        }
    }
}