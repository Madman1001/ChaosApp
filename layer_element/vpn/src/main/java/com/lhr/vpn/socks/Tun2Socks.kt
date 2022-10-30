package com.lhr.vpn.socks

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.lhr.vpn.socks.channel.StreamChannel
import com.lhr.vpn.ext.toBinString
import com.lhr.vpn.ext.toHexString
import com.lhr.vpn.socks.handle.TcpPacketHandle
import com.lhr.vpn.socks.handle.UdpPacketHandle
import com.lhr.vpn.socks.net.IP_VERSION_4
import com.lhr.vpn.socks.net.IP_VERSION_6
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.socket.ITunSocket
import com.lhr.vpn.socks.socket.ProxySession
import com.lhr.vpn.socks.socket.TcpTunSocket
import com.lhr.vpn.socks.socket.UdpTunSocket
import kotlinx.coroutines.*

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: Tun2Socks 门面类，负责ip包传递
 */
class Tun2Socks(
    private val tunInterface: ParcelFileDescriptor,
    private val vpnService: VpnService
) {
    private val tag = this::class.java.simpleName

    private val socksScope = CoroutineScope(Dispatchers.IO + Job())

    private val appTun by lazy { Tun2Tap(tunInterface) }

    //tcp 数据处理
    private val tcpPacketHandle by lazy { TcpPacketHandle() }

    //udp 数据处理
    private val udpPacketHandleList by lazy { UdpPacketHandle() }

    private val tunChannel = StreamChannel(appTun)

    @Volatile
    private var workJob: Job? = null

    @Synchronized
    fun startProxy() { //开始数据代理
        tunChannel.openChannel(socksScope)
        startWorkJob()
    }

    @Synchronized
    fun stopProxy() { //停止数据代理
        workJob?.cancel()
        workJob = null
        tunChannel.closeChannel()
        tunInterface.close()
        SessionTable.clearAllSession()
        socksScope.cancel()
    }

    /**
     * 写入数据到虚拟网卡中
     */
    fun sendData(ipPacket: NetIpPacket) {
        //Log.d(tag, "write ip packet:$ipPacket")
        tunChannel.send(ipPacket.encodePacket().array())
    }

    private fun startWorkJob(){
        if (workJob?.isActive == true) return
        workJob = socksScope.launch(Dispatchers.IO){
            while (isActive){
                val data = tunChannel.receive()
                if (data.isEmpty()) continue

                kotlin.runCatching {
                    val ipVersion = ((data[0].toUByte().toInt()) and 0xf0) ushr 4
                    when(ipVersion){
                        IP_VERSION_4 -> receiveIpV4(data)
                        IP_VERSION_6 -> receiveIpV6(data)
                        else -> {
                            Log.d(tag, "ip data: ${data.toHexString()}")
                        }
                    }
                }.onFailure {
                    Log.d(tag, "ip data: ${data.toBinString()}")
                    it.printStackTrace()
                }
            }
        }
    }

    /**
     * 转发ipv4数据
     */
    private fun receiveIpV4(data: ByteArray){
        val headerLength = (data[0].toUByte().toInt() and 0x0f)
        if (headerLength <= 0) {
            return
        }
        val packet = NetIpPacket(data)
//        Log.d(tag, "read ip packet:$packet")
        if (packet.data.isEmpty()) {
            return
        }
        //传递ip数据包
        if (!packet.isUdp() && !packet.isTcp()) return

        var session = obtainSession(packet)
        if (session == null){
            session = registerSession(packet, vpnService, this)
        }
        if (session.state == ProxySession.STATE_VALID){
            session.proxyTunSocket?.handlePacket(packet)
        }
    }

    /**
     * 接收到ipv6数据
     */
    private fun receiveIpV6(data: ByteArray){
//        Log.d(tag, "ip data: ${data.toHexString()}")
    }

    private fun obtainSession(packet: NetIpPacket): ProxySession? {
        val source = packet.sourceAddress
        val target = packet.targetAddress
        val sourcePort =
            packet.data[0].toUByte().toInt() shl 8 or packet.data[1].toUByte().toInt()
        val targetPort =
            packet.data[2].toUByte().toInt() shl 8 or packet.data[3].toUByte().toInt()
        val key = ProxySession.createSessionKey(source, sourcePort, target, targetPort)
        if (packet.isUdp()){
            return SessionTable.obtainUdpSession(key)
        }
        if (packet.isTcp()){
            return SessionTable.obtainTcpSession(key)
        }
        return null
    }

    private fun registerSession(packet: NetIpPacket, vpnService: VpnService, tun2Socks: Tun2Socks): ProxySession{
        if (!packet.isTcp() && !packet.isUdp()) {
            throw RuntimeException("packet is not udp or tcp packet")
        }
        val source = packet.sourceAddress
        val target = packet.targetAddress
        val sourcePort =
            packet.data[0].toUByte().toInt() shl 8 or packet.data[1].toUByte().toInt()
        val targetPort =
            packet.data[2].toUByte().toInt() shl 8 or packet.data[3].toUByte().toInt()

        val session = ProxySession(source, sourcePort, target, targetPort)

        var sock: ITunSocket? = null
        if (packet.isTcp()){
            sock = TcpTunSocket(session, vpnService, tun2Socks).apply {
                session.localKey = this.localKey
            }
            session.type = ProxySession.TYPE_TCP
            session.proxyTunSocket = sock
            SessionTable.registerTcpSession(session.mainKey, session)
            SessionTable.registerTcpSession(session.localKey, session)
        } else if (packet.isUdp()){
            sock = UdpTunSocket(session, vpnService, tun2Socks).apply {
                session.localKey = this.localKey
            }
            session.type = ProxySession.TYPE_UDP
            session.proxyTunSocket = sock
            SessionTable.registerUdpSession(session.mainKey, session)
            SessionTable.registerUdpSession(session.localKey, session)

        }
        Log.e(tag, "registerSession $sock")
        return session
    }

    fun unregisterSession(session: ProxySession){
        session.state = ProxySession.STATE_INVALID
        when (session.type){
            ProxySession.TYPE_TCP -> {
                SessionTable.unregisterTcpSession(session.mainKey)?.proxyTunSocket?.close()
                SessionTable.unregisterTcpSession(session.localKey)?.proxyTunSocket?.close()
            }
            ProxySession.TYPE_UDP -> {
                SessionTable.unregisterUdpSession(session.mainKey)?.proxyTunSocket?.close()
                SessionTable.unregisterUdpSession(session.localKey)?.proxyTunSocket?.close()
            }
        }
    }
}