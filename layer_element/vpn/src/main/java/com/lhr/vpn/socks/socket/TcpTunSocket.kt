package com.lhr.vpn.socks.socket

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.channel.StreamChannel
import com.lhr.vpn.ext.toHexString
import com.lhr.vpn.ext.isInsideToOutside
import com.lhr.vpn.ext.isOutsideToInside
import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.SessionTable
import com.lhr.vpn.socks.Tun2Socks
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetTcpPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class TcpTunSocket(
    val bean: ProxyRouteSession,
    val vpnService: VpnService,
    private val tunSocks: Tun2Socks,
) : ITunSocket {
    private val tag = this::class.java.simpleName

    //remote proxy socket
    private val remoteSocket = Socket().apply {
        this.bind(null)
        vpnService.protect(this)
    }
    private var remoteChannel: StreamChannel<ByteArray>? = null
    private var remoteConnectJob: Job? = null

    //local proxy socket
    private val localServerSocket = ServerSocket(0)
    private var localSocket: Socket? = null
    private var localChannel: StreamChannel<ByteArray>? = null
    private var localConnectJob: Job? = null

    @Volatile
    private var isOver = false
    @Volatile
    private var isLocalSocketClose = false
    @Volatile
    private var isRemoteSocketClose = false

    override fun handlePacket(packet: NetIpPacket) {
        if (bean.state == ProxyRouteSession.STATE_INVALID) return

        RunPool.execute(TunRunnable("$tag$this-out") {
            val tcpPacket = NetTcpPacket(packet.data)
            if (packet.isInsideToOutside()) {
                //inside to outside
                val packetSourcePort = tcpPacket.sourcePort.toUShort().toInt()
                val packetTargetPort = tcpPacket.targetPort.toUShort().toInt()
                val sourceSocketPort = bean.sourcePort
                val localSocketPort = localServerSocket.localPort
                if (packetSourcePort == localSocketPort) {
                    forwardToAppSocket(packet, tcpPacket)
                } else if (packetSourcePort == sourceSocketPort) {
                    forwardToLocalSocket(packet, tcpPacket)
                }
            } else if (packet.isOutsideToInside()) {
                //outside to inside
            }
        })
        if (!isLocalSocketClose
            && localChannel?.isOpened != true
            && localConnectJob?.isActive != true) {
            startLocalJob()
        }
        if (!isRemoteSocketClose
            && remoteChannel?.isOpened != true
            && remoteConnectJob?.isActive != true) {
            startRemoteJob()
        }
    }

    /**
     * 通过虚拟网卡转发到app socket
     */
    private fun forwardToAppSocket(packet: NetIpPacket, tcpPacket: NetTcpPacket) {
        tcpPacket.targetPort = bean.sourcePort.toShort()
        tcpPacket.sourcePort = bean.targetPort.toShort()
        packet.sourceAddress = bean.targetAddress
        packet.targetAddress = bean.sourceAddress
        packet.data = tcpPacket.encodePacket().array()
        Log.d(tag, "forwardToAppSocket $packet $tcpPacket")
        tunSocks.sendData(packet)
    }

    /**
     * 通过虚拟网卡转发到local socket
     */
    private fun forwardToLocalSocket(packet: NetIpPacket, tcpPacket: NetTcpPacket) {
        //set local port
        tcpPacket.targetPort = localServerSocket.localPort.toShort()
        tcpPacket.sourcePort = bean.targetPort.toShort()
        packet.sourceAddress = bean.targetAddress
        packet.targetAddress = bean.sourceAddress
        packet.data = tcpPacket.encodePacket().array()
        Log.d(tag, "forwardToLocalSocket $packet $tcpPacket")
        tunSocks.sendData(packet)
    }

    /**
     * 启动Local接收线程
     */
    private fun startLocalJob() {
        localChannel?.closeChannel()
        localChannel = null

        val inputRunnable = TunRunnable("$tag$this-local") {
            localSocket = localServerSocket.accept()
            val socket = localSocket ?: return@TunRunnable
            val input = socket.getInputStream()
            val output = socket.getOutputStream()
            val localReceiveBuffer = ByteArray(1024)
            localChannel = object : StreamChannel<ByteArray>() {
                override fun writeData(o: ByteArray) {
                    if (socket.isOutputShutdown){
                        closeLocalSocket()
                        Object().wait()
                        return
                    }

                    Log.e(tag, "localSocket send ${o.toHexString()}")
                    output.write(o)
                }

                override fun readData() {
                    val len = input.read(localReceiveBuffer)
                    if (len == -1){
                        Log.e(tag, "${Thread.currentThread().name}===================localSocket close=================")
                        closeLocalSocket()
                        tunSocks.unregisterSession(bean)
                        Object().wait()
                        return
                    }
                    if (len > 0) {
                        val data = ByteArray(len)
                        System.arraycopy(localReceiveBuffer, 0, data, 0, data.size)
                        Log.e(tag, "localSocket receive ${data.toHexString()}")
                        remoteChannel?.sendData(data)
                    } else {
                        Thread.sleep(100)
                    }
                }
            }
            localChannel?.openChannel()
        }
        localConnectJob = GlobalScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                inputRunnable.run()
            }.onFailure {
                it.printStackTrace()
                // 失败直接移除
                tunSocks.unregisterSession(bean)
            }
        }
    }

    /**
     * 启动Remote接收线程
     */
    private fun startRemoteJob() {
        remoteChannel?.closeChannel()
        remoteChannel = null

        val inputRunnable = TunRunnable("$tag$this-remote") {
            if (!remoteSocket.isConnected) {
                remoteSocket.connect(InetSocketAddress(bean.targetAddress, bean.targetPort), 3000)
            }
            val input = remoteSocket.getInputStream()
            val output = remoteSocket.getOutputStream()
            val remoteReceiveBuffer = ByteArray(1024)
            remoteChannel = object : StreamChannel<ByteArray>() {
                override fun writeData(o: ByteArray) {
                    if (remoteSocket.isOutputShutdown){
                        Object().wait()
                        return
                    }

                    Log.e(tag, "remoteSocket send ${o.toHexString()}")
                    output.write(o)
                }

                override fun readData() {
                    val len = input.read(remoteReceiveBuffer)
                    if (len == -1){
                        Log.e(tag, "${Thread.currentThread().name}===================remoteSocket close=================")
                        closeRemoteSocket()
                        Object().wait()
                        return
                    }
                    if (len > 0) {
                        val data = ByteArray(len)
                        System.arraycopy(remoteReceiveBuffer, 0, data, 0, data.size)
                        Log.e(tag, "remoteSocket receive ${data.toHexString()}")
                        localChannel?.sendData(data)
                    } else {
                        Thread.sleep(100)
                    }
                }
            }
            remoteChannel?.openChannel()
        }
        remoteConnectJob = GlobalScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                inputRunnable.run()
            }.onFailure {
                it.printStackTrace()
                // 失败直接移除
                tunSocks.unregisterSession(bean)
            }
        }
    }

    override fun createLocalKey(): String {
        return SessionTable.createSessionKey(
            bean.sourceAddress,
            localServerSocket.localPort,
            bean.targetAddress,
            bean.targetPort
        )
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("\n").append("source port ").append(bean.sourcePort)
        sb.append("\n").append("target port ").append(bean.targetPort)
        sb.append("\n").append("local socket port ").append(localServerSocket.localPort)
        sb.append("\n").append("remote socket port ").append(remoteSocket.localPort)
        return sb.toString()
    }

    override fun close() {
        closeLocalSocket()

        closeRemoteSocket()

        isOver = true

        Log.e(tag, "close tcp socket ${localServerSocket.localPort} ${remoteSocket.localPort}")
    }

    private fun closeRemoteSocket(){
        kotlin.runCatching {
            remoteConnectJob?.cancel()
            remoteConnectJob = null
            remoteChannel?.closeChannel()
            remoteChannel = null
        }

        kotlin.runCatching {
            remoteSocket.shutdownInput()
            remoteSocket.shutdownOutput()
        }
        kotlin.runCatching {
            remoteSocket.close()
        }
        isRemoteSocketClose = true
    }

    private fun closeLocalSocket(){
        kotlin.runCatching {
            localConnectJob?.cancel()
            localConnectJob = null
            localChannel?.closeChannel()
            localChannel = null
        }

        kotlin.runCatching {
            localSocket?.shutdownInput()
            localSocket?.shutdownOutput()
        }
        kotlin.runCatching {
            localSocket?.close()
            localSocket = null
        }
        kotlin.runCatching {
            localServerSocket.close()
        }
        isLocalSocketClose = true
    }
}