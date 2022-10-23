package com.lhr.vpn.socks

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.lhr.vpn.channel.StreamChannel
import com.lhr.vpn.ext.toBinString
import com.lhr.vpn.ext.toHexString
import com.lhr.vpn.socks.handle.TcpPacketHandle
import com.lhr.vpn.socks.handle.UdpPacketHandle
import com.lhr.vpn.socks.net.IP_VERSION_4
import com.lhr.vpn.socks.net.IP_VERSION_6
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.socket.ITunSocket
import com.lhr.vpn.socks.socket.ProxyRouteSession
import com.lhr.vpn.socks.socket.TcpTunSocket
import com.lhr.vpn.socks.socket.UdpTunSocket

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

    private val appTun by lazy { Tun2Tap(tunInterface) }

    //tcp 数据处理
    private val tcpPacketHandle by lazy { TcpPacketHandle() }

    //udp 数据处理
    private val udpPacketHandleList by lazy { UdpPacketHandle() }

    private val tunChannel = object : StreamChannel<ByteArray>() {
        override fun writeData(o: ByteArray) {
            this@Tun2Socks.writeDataToTun(o)
        }

        override fun readData() {
            this@Tun2Socks.receiveDataByTun()
        }
    }

    @Synchronized
    fun startProxy() { //开始数据代理
        tunChannel.openChannel()
    }

    @Synchronized
    fun stopProxy() { //停止数据代理
        tunChannel.closeChannel()
        tunInterface.close()
        SessionTable.clearAllSession()
    }

    fun sendData(ipPacket: NetIpPacket) {
        synchronized(tunChannel) {
//            Log.d(tag, "write ip packet:$ipPacket")
            tunChannel.sendData(ipPacket.encodePacket().array())
        }
    }

    /**
     * 读取数据然后进行分发
     */
    private fun receiveDataByTun() {
        val data = appTun.readTun()
        if (data.isEmpty()) return

        kotlin.runCatching {
            val ipVersion = ((data[0].toUByte().toInt()) and 0xf0) ushr 4
            when(ipVersion){
                IP_VERSION_4 -> receiveIpV4(data)
                IP_VERSION_6 -> receiveIpV6(data)
                else -> {
                    Log.d(tag, "ip data: ${data.toBinString()}")
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    /**
     * 转发ipv4数据
     */
    private fun receiveIpV4(data: ByteArray){
        val headerLength = (data[0].toUByte().toInt() and 0x0f)
        //非ip v4 包
        if (headerLength <= 0) {
            return
        }
        val packet = NetIpPacket(data)
        Log.d(tag, "read ip packet:$packet")
        if (packet.data.isEmpty()) {
            return
        }
        //传递ip数据包
        if (!packet.isUdp() && !packet.isTcp()) return

        var session = obtainSession(packet)
        if (session == null){
            session = registerSession(packet, vpnService, this)
        }
        if (session.state == ProxyRouteSession.STATE_VALID){
            session.proxyTunSocket?.handlePacket(packet)
        }
    }

    /**
     * 接收到ipv6数据
     */
    private fun receiveIpV6(data: ByteArray){
        Log.d(tag, "ip data: ${data.toHexString()}")
    }

    /**
     * 写入数据到虚拟网卡中
     */
    private fun writeDataToTun(data: ByteArray) {
        if (data.isEmpty()) return
        kotlin.runCatching {
            appTun.writeTun(data)
        }
    }

    fun obtainSession(packet: NetIpPacket): ProxyRouteSession? {
        val source = packet.sourceAddress
        val target = packet.targetAddress
        val sourcePort =
            packet.data[0].toUByte().toInt() shl 8 or packet.data[1].toUByte().toInt()
        val targetPort =
            packet.data[2].toUByte().toInt() shl 8 or packet.data[3].toUByte().toInt()
        val key = SessionTable.createSessionKey(source, sourcePort, target, targetPort)
        if (packet.isUdp()){
            return SessionTable.obtainUdpSession(key)
        }
        if (packet.isTcp()){
            return SessionTable.obtainTcpSession(key)
        }
        return null
    }

    private fun registerSession(packet: NetIpPacket, vpnService: VpnService, tun2Socks: Tun2Socks): ProxyRouteSession{
        if (!packet.isTcp() && !packet.isUdp()) {
            throw RuntimeException("packet is not udp or tcp packet")
        }
        val source = packet.sourceAddress
        val target = packet.targetAddress
        val sourcePort =
            packet.data[0].toUByte().toInt() shl 8 or packet.data[1].toUByte().toInt()
        val targetPort =
            packet.data[2].toUByte().toInt() shl 8 or packet.data[3].toUByte().toInt()

        val session = ProxyRouteSession(source, sourcePort, target, targetPort)
        val key = SessionTable.createSessionKey(
            session.sourceAddress,
            session.sourcePort,
            session.targetAddress,
            session.targetPort
        )
        val key2: String
        val sock: ITunSocket?
        if (packet.isTcp()){
            sock = TcpTunSocket(session, vpnService, tun2Socks)
            key2 = sock.createLocalKey()
            session.proxyTunSocket = sock
            SessionTable.registerTcpSession(key, session)
            SessionTable.registerTcpSession(key2, session)
        } else if (packet.isUdp()){
            sock = UdpTunSocket(session, vpnService, tun2Socks)
            key2 = sock.createLocalKey()
            session.proxyTunSocket = sock
            SessionTable.registerUdpSession(key, session)
            SessionTable.registerUdpSession(key2, session)

        }

        return session
    }

    fun unregisterSession(session: ProxyRouteSession){
        if (session.proxyTunSocket is TcpTunSocket){
            SessionTable.unregisterTcpSession(session)
        } else if (session.proxyTunSocket is UdpTunSocket){
            SessionTable.unregisterUdpSession(session)
        }
    }
}