package com.lhr.vpn.socks.socket

import android.util.Log
import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.NetProxyBean
import com.lhr.vpn.socks.TcpSocks
import com.lhr.vpn.socks.net.v4.NetTcpPacket
import com.lhr.vpn.socks.net.v4.proto.*
import com.lhr.vpn.socks.socket.action.ITcpAction
import com.lhr.vpn.socks.socket.action.TunTcpServer
import com.lhr.vpn.util.PacketV4Factory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class TcpTunSocket(
    val bean: NetProxyBean,
    private val tcpSocks: TcpSocks,
    internal val socket: Socket
) {
    private val tag = this::class.java.simpleName

    private var receiveJob: Job? = null

    private var sendJob: Job? = null

    private val receiveBuffer = ByteArray(1024)

    private val sendQueue = LinkedBlockingDeque<NetTcpPacket>()

    private var tunTcpServer: ITcpAction = TunTcpServer(this)

    fun sendPacket(packet: NetTcpPacket) {
        Log.d(tag, "sendPacket $packet")
        sendQueue.putLast(packet)
        if (sendJob == null || sendJob?.isActive != true){
            startSend()
        }
    }

    fun receivePacket(packet: NetTcpPacket){
        Log.d(tag, "receivePacket $packet")
        tcpSocks.socksToTun(bean, packet)
    }

    /**
     * 启动接收线程
     */
    internal fun startSend() {
        sendJob?.cancel()

        val outputRunnable = TunRunnable("$tag$this-out") {
            while (true) {
                val packet = sendQueue.takeFirst() ?: return@TunRunnable
                tunTcpServer.send(packet)
            }
        }
        sendJob = GlobalScope.launch(Dispatchers.IO) {
            outputRunnable.run()
        }
    }

    /**
     * 启动接收线程
     */
    internal fun startReceive() {
        receiveJob?.cancel()

        val inputRunnable = TunRunnable("$tag$this-in") {
            val inputStream = socket.getInputStream()
            while (true) {
                val len = inputStream.read(receiveBuffer)
                val data = ByteArray(len)
                System.arraycopy(receiveBuffer, 0, data, 0, len)
                val tcpPacket = PacketV4Factory.createTcpPacket(
                    data = data,
                    sourcePort = bean.targetPort,
                    targetPort = bean.sourcePort
                )
                receivePacket(tcpPacket)
            }
        }
        receiveJob = GlobalScope.launch(Dispatchers.IO) {
            inputRunnable.run()
        }
    }
}