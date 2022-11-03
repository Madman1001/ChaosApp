package com.lhr.vpn.socks

import android.util.Log
import com.lhr.vpn.*
import com.lhr.vpn.socks.net.IP_VERSION_4
import com.lhr.vpn.socks.net.IP_VERSION_6
import com.lhr.vpn.socks.net.MAX_PACKET_SIZE
import com.lhr.vpn.socks.net.v4.NetPacket
import com.lhr.vpn.socks.proxy.ProxySession
import com.lhr.vpn.socks.proxy.TcpProxyServer
import com.lhr.vpn.socks.proxy.UdpProxyServer
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.InetSocketAddress

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: Tun2Socks 负责ip包传递
 */
class TunSocks(
    private val vpnService: LocalVpnService
) {
    private val tag = this::class.java.simpleName

    private val socksScope = CoroutineScope(Dispatchers.IO + Job())

    private val tunOutput by lazy { FileOutputStream(vpnService.tunInterface.fileDescriptor) }

    private val tunInput by lazy { FileInputStream(vpnService.tunInterface.fileDescriptor) }

    private val proxyTcpServer by lazy { TcpProxyServer(vpnService, socksScope) }

    private val proxyUdpServer by lazy { UdpProxyServer(vpnService, this, socksScope) }

    private val hostIp = LocalVpnConfig.PROXY_ADDRESS.toIpInt()

    @Volatile
    private var workJob: Job? = null

    fun startProxy() { //开始数据代理
        startWorkJob()
        proxyTcpServer.startProxy()
        proxyUdpServer.startProxy()
    }

    fun stopProxy() { //停止数据代理
        socksScope.cancel()
        proxyTcpServer.stopProxy()
        proxyUdpServer.stopProxy()
        vpnService.tunInterface.close()
    }

    fun sendTunData(byteArray: ByteArray, offset: Int = 0, len: Int = byteArray.size){
        tunOutput.write(byteArray, offset, len)
        tunOutput.flush()
    }

    private fun startWorkJob(){
        if (workJob?.isActive == true) return
        workJob = socksScope.launch(Dispatchers.IO){
            kotlin.runCatching {
                val byteArray = ByteArray(MAX_PACKET_SIZE)
                tunInput.use {
                    while (isActive){
                        val len = it.read(byteArray)
                        if (!onReceive(byteArray, len)){
                            delay(100)
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private fun onReceive(byteArray: ByteArray, len: Int): Boolean{
        if (len <= 0) return false
        val ipVersion = ((byteArray[0].toNetInt()) and 0xf0) ushr 4
        return when(ipVersion){
            IP_VERSION_4 -> receiveIpV4(byteArray, len)
            IP_VERSION_6 -> receiveIpV6(byteArray, len)
            else -> {
                Log.d(tag, "ip  data: ${byteArray.toHexString()}")
                false
            }
        }
    }

    /**
     * 转发ipv4数据
     */
    private fun receiveIpV4(data: ByteArray, len: Int): Boolean{
        val headerLength = (data[0].toNetInt() and 0x0f)
        if (headerLength <= 0) {
            return false
        }
        val packet = NetPacket(data)
        Log.d(tag, "read ip packet:${packet.ipHeader}")
        if (packet.ipHeader.sourceIp != hostIp) return false
        if (!packet.checkChecksum()) {
            throw RuntimeException("")
        }
        //传递ip数据包
        return if (packet.isTcp()){
            Log.d(tag, "read tcp packet:${packet.tcpHeader}")
            if (packet.tcpHeader.sourcePort == proxyTcpServer.serverPort){
                val session = proxyTcpServer.tcpSessions[packet.tcpHeader.destinationPort] ?: return false
                val sourceIp = packet.ipHeader.sourceIp
                packet.ipHeader.sourceIp = packet.ipHeader.destinationIp
                packet.tcpHeader.sourcePort = session.port
                packet.ipHeader.destinationIp = sourceIp
                packet.setChecksum()
                sendTunData(packet.rawData, 0, len)
            } else {
                val port = packet.tcpHeader.sourcePort
                proxyTcpServer.tcpSessions[port]?.takeIf {
                    it.address == packet.ipHeader.destinationIp && it.port == packet.tcpHeader.destinationPort
                } ?: ProxySession(packet.ipHeader.destinationIp, packet.tcpHeader.destinationPort).also {
                    it.type = ProxySession.TYPE_TCP
                    proxyTcpServer.tcpSessions[port] = it
                }
                val sourceIp = packet.ipHeader.sourceIp
                packet.ipHeader.sourceIp = packet.ipHeader.destinationIp
                packet.ipHeader.destinationIp = sourceIp
                packet.tcpHeader.destinationPort = proxyTcpServer.serverPort
                packet.setChecksum()
                sendTunData(packet.rawData, 0, len)
            }
            true
        } else if (packet.isUdp()){
            Log.d(tag, "read udp packet:${packet.udpHeader}")
            val datagramPacket = DatagramPacket(packet.data, packet.data.size, InetSocketAddress(packet.ipHeader.destinationIp.toIpString(), packet.udpHeader.destinationPort.toNetInt()))
            proxyUdpServer.sendData(packet.udpHeader.sourcePort, datagramPacket)
            true
        } else {
            false
        }
    }

    /**
     * 接收到ipv6数据
     */
    private fun receiveIpV6(data: ByteArray, len: Int): Boolean{
//        Log.d(tag, "ip data: ${data.toHexString()}")
        return false
    }
}