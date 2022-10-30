package com.lhr.vpn.socks.socket

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.socks.channel.TcpTunChannel
import com.lhr.vpn.ext.isInsideToOutside
import com.lhr.vpn.ext.toHexString
import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.Tun2Socks
import com.lhr.vpn.socks.channel.BaseTunChannel
import com.lhr.vpn.socks.net.*
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetTcpPacket
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class TcpTunSocket(
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

    //remote proxy socket
    @Volatile
    private var remoteJob: Job? = null
    private val remoteSocket = Socket().apply {
        this.bind(null)
        vpnService.protect(this)
    }
    private val remoteChannel by lazy { TcpTunChannel(remoteSocket, "remote") }

    //local proxy socket
    @Volatile
    private var localJob: Job? = null
    private val localServerSocket = ServerSocket(0)
    @Volatile
    private var localChannel: TcpTunChannel? = null

    private val localServerAcceptJob: Job =
        socketScope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
            val socket = localServerSocket.accept() ?: return@launch
            startRemoteJob()
            startLocalJob(socket)
        }

    override fun handlePacket(packet: NetIpPacket) {
        RunPool.execute(TunRunnable("$tag$this-out") {
            val tcpPacket = NetTcpPacket(packet.data)
            if (packet.isInsideToOutside()) {
                //inside to outside
                val packetSourcePort = tcpPacket.sourcePort.toUShort().toInt()
                val packetTargetPort = tcpPacket.targetPort.toUShort().toInt()
                val sourceSocketPort = proxySession.sourcePort
                val localSocketPort = localServerSocket.localPort
                if (packetSourcePort == localSocketPort) {
                    forwardToAppSocket(packet, tcpPacket)
                    Log.d(tag, "forwardToAppSocket ${packetFormat(packet, tcpPacket)}")
                } else if (packetSourcePort == sourceSocketPort) {
                    forwardToLocalSocket(packet, tcpPacket)
                    Log.d(tag, "forwardToLocalSocket ${packetFormat(packet, tcpPacket)}")
                }

                if (!isValid) return@TunRunnable
                if (tcpPacket.controlSign and SIGN_SYN != 0){
                    if (!localServerAcceptJob.isActive || localChannel == null){
                        localServerAcceptJob.start()
                    }
                }
            }
        })
    }

    /**
     * 通过虚拟网卡转发到app socket
     */
    private fun forwardToAppSocket(packet: NetIpPacket, tcpPacket: NetTcpPacket) {
        tcpPacket.targetPort = proxySession.sourcePort.toShort()
        tcpPacket.sourcePort = proxySession.targetPort.toShort()
        packet.sourceAddress = proxySession.targetAddress
        packet.targetAddress = proxySession.sourceAddress
        packet.data = tcpPacket.encodePacket().array()
        tunSocks.sendData(packet)
    }

    /**
     * 通过虚拟网卡转发到local socket
     */
    private fun forwardToLocalSocket(packet: NetIpPacket, tcpPacket: NetTcpPacket) {
        //set local port
        tcpPacket.targetPort = localServerSocket.localPort.toShort()
        packet.targetAddress = proxySession.sourceAddress
        tcpPacket.sourcePort = proxySession.targetPort.toShort()
        packet.sourceAddress = proxySession.targetAddress
        packet.data = tcpPacket.encodePacket().array()

        tunSocks.sendData(packet)
    }

    /**
     * 启动Local接收线程
     */
    private fun startLocalJob(socket: Socket) {
        if (localChannel == null){
            localChannel = TcpTunChannel(socket, "local")
            localJob?.cancel()
            localJob = null
        }
        val channel = localChannel ?: return
        if (localJob?.isActive != true){
            localJob = socketScope.launch(Dispatchers.IO){
                while (isActive){
                    val data = channel.receive()
                    if (data === BaseTunChannel.CloseSign){
                        break
                    }
                    if (data.isNotEmpty()){
                        Log.e(tag, "localChannel receive ${data.size}Byte")
                        remoteChannel.send(data)
                    }
                }
                Log.d(tag, "localJob over")
                remoteChannel.send(BaseTunChannel.CloseSign)
                closeOrWaitClose()
            }
        }
        channel.openChannel(socketScope)
    }

    /**
     * 启动Remote接收线程
     */
    private fun startRemoteJob() {
        if (remoteJob?.isActive != true){
            remoteJob = socketScope.launch(Dispatchers.IO){
                if (!remoteSocket.isConnected) {
                    remoteSocket.connect(InetSocketAddress(proxySession.targetAddress, proxySession.targetPort), 3000)
                }
                remoteChannel.openChannel(socketScope)
                while (isActive){
                    val data = remoteChannel.receive()
                    if (data === BaseTunChannel.CloseSign){
                        break
                    }
                    if (data.isNotEmpty()){
                        localChannel?.send(data)
                    }
                    Log.e(tag, "remoteChannel receive ${data.size}Byte")
                }
                Log.d(tag, "remoteJob over")
                localChannel?.send(BaseTunChannel.CloseSign)
                closeOrWaitClose()
            }
        }
    }

    private fun closeOrWaitClose(){
        isWaitClose = true
        if (localChannel?.isConnected() != true && !remoteChannel.isConnected()){
            close()
        }
    }

    override fun close() {
        if (!isValid) return
        isValid = false

        socketScope.cancel()

        if (!localServerSocket.isClosed){
            localServerSocket.close()
        }
        if (!remoteSocket.isClosed){
            remoteSocket.close()
        }
        Log.e(tag, "close ${toString()}")
    }

    private fun packetFormat(ipPacket: NetIpPacket, tcpPacket: NetTcpPacket): String{
        val sb = StringBuilder()
        sb.append("${ipPacket.sourceAddress.hostAddress}:${tcpPacket.sourcePort.toUShort()} >> ${ipPacket.targetAddress.hostAddress}:${tcpPacket.targetPort.toUShort()}")
            .append("\n [ ")
            .append(if (tcpPacket.controlSign and SIGN_URG != 0) "URG " else "")
            .append(if (tcpPacket.controlSign and SIGN_ACK != 0) "ACK " else "")
            .append(if (tcpPacket.controlSign and SIGN_PSH != 0) "PSH " else "")
            .append(if (tcpPacket.controlSign and SIGN_RST != 0) "RST " else "")
            .append(if (tcpPacket.controlSign and SIGN_SYN != 0) "SYN " else "")
            .append(if (tcpPacket.controlSign and SIGN_FIN != 0) "FIN " else "")
            .append(" ]")
            .append("\n Seq=${tcpPacket.sequenceNumber.toUInt()}")
            .append("\n Ack=${tcpPacket.ackSequenceNumber.toUInt()}")
            .append("\n Win=${tcpPacket.windowSize}")
            .append("\n Len=${tcpPacket.data.size}")
        return sb.toString()
    }

    override fun toString(): String {
        return desc
    }

    val desc: String

    val localKey: String

    init {
        val sb = StringBuilder()
        sb.append("TcpTunSocket$").append(this.hashCode())
        sb.append("\n").append("local: ${proxySession.sourcePort} <-> ${localServerSocket.localPort}")
        sb.append("\n").append("remote: ${proxySession.sourceAddress.hostAddress}:${remoteSocket.localPort} <-> ${proxySession.targetAddress.hostAddress}:${proxySession.targetPort}")
        desc = sb.toString()

        localKey = ProxySession.createSessionKey(
            proxySession.sourceAddress,
            localServerSocket.localPort,
            proxySession.targetAddress,
            proxySession.targetPort
        )
    }
}