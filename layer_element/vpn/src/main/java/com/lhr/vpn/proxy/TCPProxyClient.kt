package com.lhr.vpn.proxy

import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.proxy.state.SocketAction
import com.lhr.vpn.proxy.state.TCPListenAction
import com.lhr.vpn.proxy.state.UselessAction
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import kotlin.random.Random

/**
 * @author lhr
 * @date 2021/12/4
 * @des udp 客户端代理类
 */
class TCPProxyClient(
    private val handleTun: IProxyTun,
    private val tcpSocket: Socket,
    internal val proxyConfig: ProxyConfig
) : IProxyClient {
    private val tag = "TCPProxyClient"
    private val packetList = Vector<TCPPacket>()

    private var currentTcpSendThread: Thread? = null

    private var currentTcpReceiveThread: Thread? = null

    internal var serverSerialNumber = 0L

    internal var clientSerialNumber = 0L

    @Volatile
    internal var action: SocketAction = UselessAction(tag)

    /**
     * 连接的最大等待时长
     */
    internal var liveTime: Long = Long.MAX_VALUE

    internal var windowSize: Int = 0

    @Volatile
    private var isStart = false

    @Synchronized
    fun start() {
        if (!isStart) {
            action = TCPListenAction(this)
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
        if (isStart) {
            isStart = false
            action = UselessAction(tag)
            if (currentTcpSendThread != null) {
                if (currentTcpSendThread?.state == Thread.State.TIMED_WAITING) {
                    currentTcpSendThread?.interrupt()
                }
            }

            if (currentTcpReceiveThread != null) {
                if (currentTcpReceiveThread?.state == Thread.State.TIMED_WAITING) {
                    currentTcpReceiveThread?.interrupt()
                }
            }
        }

        if (tcpSocket.isConnected){
            tcpSocket.shutdownInput()
            tcpSocket.shutdownOutput()
            tcpSocket.close()
        }
    }

    fun pushPacket(packet: TCPPacket) {
        if (!isStart) {
            start()
        }

        packetList.add(packet)
        if (currentTcpSendThread?.state == Thread.State.TIMED_WAITING) {
            currentTcpSendThread?.interrupt()
        }
    }

    private fun sendRun() {
        currentTcpSendThread = Thread.currentThread()
        while (isStart) {
            if (packetList.isNotEmpty()) {
                val packet = packetList.removeFirst()
                action.send(packet)
            } else {
                try {
                    Thread.sleep(liveTime)
                    break
                } catch (e: InterruptedException) {
                }
                Log.e(tag, "线程被唤醒")
            }
        }
    }

    private fun receiveRun() {
        currentTcpReceiveThread = Thread.currentThread()
        Log.d(tag, "start proxy tcp receive")
        while (isStart) {
            if (tcpSocket.isConnected
                && !tcpSocket.isInputShutdown
            ) {
                try {
                    val buffer = ByteArray(tcpSocket.receiveBufferSize)
                    val input = tcpSocket.getInputStream()
                    val len = input.read(buffer)
                    val str = String(buffer, 0, len)
                    Log.d(tag, "proxy tcp receive:$str")
                    val packet = TCPPacket().apply {
                        this.setData(buffer, 0, len)
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
                }catch (e: Exception){

                }
            }
        }
    }

    /**
     * 连接目标服务器
     */
    internal fun connect(): Boolean {
        if (!tcpSocket.isConnected) {
            //未连接，进行三次握手
            val remoteAddr = InetSocketAddress(proxyConfig.targetAddress, proxyConfig.targetPort)
            try {
                tcpSocket.connect(remoteAddr, 3000)
            } catch (e: Exception) {
                e.printStackTrace()
                //连接失败
            }
            return tcpSocket.isConnected
        } else {
            return false
        }
    }

    override fun internalToExternal(packet: IPPacket) {
        if (tcpSocket.isConnected && !tcpSocket.isOutputShutdown) {
            val data = packet.getData()
            if (data.isNotEmpty()) {
                tcpSocket.getOutputStream().apply {
                    this.write(data)
                    this.flush()
                }
            }
        }
    }

    override fun externalToInternal(packet: IPPacket) {
        handleTun.outputData(packet)
    }
}