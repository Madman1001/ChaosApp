package com.lhr.vpn.socks.socket

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.ext.isInsideToOutside
import com.lhr.vpn.ext.toHexString
import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.SessionTable
import com.lhr.vpn.socks.Tun2Socks
import com.lhr.vpn.socks.channel.BaseTunChannel
import com.lhr.vpn.socks.channel.UdpTunChannel
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import kotlinx.coroutines.*
import java.net.DatagramSocket
import java.net.InetSocketAddress

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class UdpTunSocket(
    val proxySession: ProxySession,
    val vpnService: VpnService,
    private val tunSocks: Tun2Socks,
) : ITunSocket {
    private val tag = this::class.java.simpleName

    @Volatile
    private var isValid = true

    @Volatile
    private var isWaitClose = false

    private val socketScope = CoroutineScope(Dispatchers.IO + Job())

    //remote proxy datagram socket
    @Volatile
    private var remoteJob: Job? = null
    private val remoteSocket = DatagramSocket().apply {
        this.soTimeout = DEFAULT_UDP_SO_TIMEOUT
        vpnService.protect(this)
    }
    private val remoteChannel by lazy {
        UdpTunChannel(
            remoteSocket,
            InetSocketAddress(proxySession.targetAddress, proxySession.targetPort)
        )
    }

    //local proxy datagram socket
    @Volatile
    private var localJob: Job? = null
    private val localServerSocket = DatagramSocket().apply {
        this.soTimeout = DEFAULT_UDP_SO_TIMEOUT
    }
    private val localChannel by lazy {
        UdpTunChannel(
            localServerSocket,
            InetSocketAddress(proxySession.targetAddress, proxySession.targetPort)
        )
    }

    override fun handlePacket(packet: NetIpPacket) {
        if (!isValid) return

        RunPool.execute(TunRunnable("$tag$this-out") {
            val udpPacket = NetUdpPacket(packet.data)
            if (packet.isInsideToOutside()) {
                //inside to outside
                val packetSourcePort = udpPacket.sourcePort.toUShort().toInt()
                val packetTargetPort = udpPacket.targetPort.toUShort().toInt()
                val sourceSocketPort = proxySession.sourcePort
                val localSocketPort = localServerSocket.localPort
                if (packetSourcePort == localSocketPort) {
                    forwardToAppSocket(packet, udpPacket)
                } else if (packetSourcePort == sourceSocketPort) {
                    forwardToLocalSocket(packet, udpPacket)
                }
            }
        })

        if (!isWaitClose){
            startLocalJob()

            startRemoteJob()
        }
    }

    /**
     * 通过虚拟网卡转发到app socket
     */
    private fun forwardToAppSocket(packet: NetIpPacket, udpPacket: NetUdpPacket) {
        udpPacket.targetPort = proxySession.sourcePort.toShort()
        udpPacket.sourcePort = proxySession.targetPort.toShort()
        packet.sourceAddress = proxySession.targetAddress
        packet.targetAddress = proxySession.sourceAddress
        packet.data = udpPacket.encodePacket().array()
        Log.d(tag, "forwardToAppSocket ${packet.sourceAddress.hostAddress}:${udpPacket.sourcePort.toUShort()} >> ${packet.targetAddress.hostAddress}:${udpPacket.targetPort.toUShort()}")
        tunSocks.sendData(packet)
    }

    /**
     * 通过虚拟网卡转发到local socket
     */
    private fun forwardToLocalSocket(packet: NetIpPacket, udpPacket: NetUdpPacket) {
        //set local port
        udpPacket.targetPort = localServerSocket.localPort.toShort()
        udpPacket.sourcePort = proxySession.targetPort.toShort()
        packet.sourceAddress = proxySession.targetAddress
        packet.targetAddress = proxySession.sourceAddress
        packet.data = udpPacket.encodePacket().array()
        Log.d(tag, "forwardToLocalSocket ${packet.sourceAddress.hostAddress}:${udpPacket.sourcePort.toUShort()} >> ${packet.targetAddress.hostAddress}:${udpPacket.targetPort.toUShort()}")
        tunSocks.sendData(packet)
    }

    /**
     * 启动Local接收线程
     */
    private fun startLocalJob() {
        if (localJob?.isActive != true){
            localJob = socketScope.launch(Dispatchers.IO){
                while (isActive){
                    //接收内部数据
                    val data = localChannel.receive()
                    if (data === BaseTunChannel.CloseSign){
                        waitClose()
                        Log.d(tag, "localJob over")
                        return@launch
                    }
                    if (data.isNotEmpty()){
                        remoteChannel.send(data)
                    }
                    Log.e(tag, "localChannel receive ${data.toHexString()}")
                }
            }
        }

        localChannel.openChannel(socketScope)
    }

    /**
     * 启动Remote接收线程
     */
    private fun startRemoteJob() {
        if (remoteJob?.isActive != true) {
            remoteJob = socketScope.launch(Dispatchers.IO) {
                while (isActive) {
                    val data = remoteChannel.receive()
                    if (data === BaseTunChannel.CloseSign){
                        Log.d(tag, "remoteJob over")
                        waitClose()
                        return@launch
                    }
                    if (data.isNotEmpty()) {
                        localChannel.send(data)
                    }
                    Log.e(tag, "remoteChannel receive ${data.toHexString()}")
                }
            }
        }

        remoteChannel.openChannel(socketScope)
    }

    override fun toString(): String {
        return desc
    }

    private fun waitClose(){
        isWaitClose = true
        if (!remoteChannel.isWaitClose){
            remoteChannel.waitClose()
        }
        if (!localChannel.isWaitClose){
            localChannel.waitClose()
        }

        if (!localChannel.isValid && !remoteChannel.isValid){
            close()
        }
    }

    override fun close() {
        if (!isValid) return
        isValid = false

        proxySession.state = ProxySession.STATE_INVALID

        socketScope.cancel()

        if (!localServerSocket.isClosed){
            localServerSocket.close()
        }
        if (!remoteSocket.isClosed){
            remoteSocket.close()
        }
        tunSocks.unregisterSession(proxySession)
        Log.e(tag, "close ${toString()}")
    }


    val desc: String

    val localKey: String

    init {
        val sb = StringBuilder()
        sb.append("UdpTunSocket$").append(this.hashCode())
        sb.append("\n").append("local: ${proxySession.sourcePort} <-> ${localServerSocket.localPort}")
        sb.append("\n").append("remote: ${proxySession.sourceAddress.hostAddress}:${proxySession.sourcePort} <-> ${proxySession.targetAddress.hostAddress}:${proxySession.targetPort}")
        desc = sb.toString()

        localKey = ProxySession.createSessionKey(
            proxySession.sourceAddress,
            localServerSocket.localPort,
            proxySession.targetAddress,
            proxySession.targetPort
        )
    }

    companion object {
        const val DEFAULT_UDP_SO_TIMEOUT = 3000
    }
}