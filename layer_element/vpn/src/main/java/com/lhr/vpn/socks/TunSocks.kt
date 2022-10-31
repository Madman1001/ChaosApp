package com.lhr.vpn.socks

import android.util.Log
import com.lhr.vpn.LocalVpnConfig
import com.lhr.vpn.LocalVpnService
import com.lhr.vpn.ext.toHexString
import com.lhr.vpn.socks.net.IP_VERSION_4
import com.lhr.vpn.socks.net.IP_VERSION_6
import com.lhr.vpn.socks.net.MAX_PACKET_SIZE
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.proxy.ProxySession
import com.lhr.vpn.socks.proxy.TcpProxyServer
import com.lhr.vpn.socks.proxy.UdpProxyServer
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: Tun2Socks 门面类，负责ip包传递
 */
class TunSocks(
    private val vpnService: LocalVpnService
) {
    private val tag = this::class.java.simpleName

    private val socksScope = CoroutineScope(Dispatchers.IO + Job())

    private val tunOutput by lazy { FileOutputStream(vpnService.tunInterface.fileDescriptor) }

    private val tunInput by lazy { FileInputStream(vpnService.tunInterface.fileDescriptor) }

    private val proxyTcpServer by lazy { TcpProxyServer(vpnService, socksScope) }

    private val proxyUdpServer by lazy { UdpProxyServer(vpnService, socksScope) }

    @Volatile
    private var workJob: Job? = null

    fun startProxy() { //开始数据代理
        startWorkJob()
        proxyTcpServer.startProxy()
    }

    fun stopProxy() { //停止数据代理
        socksScope.cancel()
        proxyTcpServer.stopProxy()
        vpnService.tunInterface.close()
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
        val ipVersion = ((byteArray[0].toUByte().toInt()) and 0xf0) ushr 4
        return when(ipVersion){
            IP_VERSION_4 -> receiveIpV4(byteArray)
            IP_VERSION_6 -> receiveIpV6(byteArray)
            else -> {
                Log.d(tag, "ip data: ${byteArray.toHexString()}")
                false
            }
        }
    }

    /**
     * 转发ipv4数据
     */
    private fun receiveIpV4(data: ByteArray): Boolean{
        val headerLength = (data[0].toUByte().toInt() and 0x0f)
        if (headerLength <= 0) {
            return false
        }
        val ipPacket = NetIpPacket(data)
//        Log.d(tag, "read ip packet:$packet")
        if (ipPacket.sourceAddress.hostAddress != LocalVpnConfig.PROXY_ADDRESS) {
            return false
        }
        //传递ip数据包
        return if (ipPacket.isTcp()){
            if (ipPacket.sourcePort == proxyTcpServer.serverPort){
                val session = proxyTcpServer.tcpSessions[ipPacket.destinationPort] ?: return false
                val sourceAddr = ipPacket.sourceAddress
                ipPacket.sourceAddress = ipPacket.destinationAddress
                ipPacket.sourcePort = session.port
                ipPacket.destinationAddress = sourceAddr
                tunOutput.write(ipPacket.encodePacket().array())
            } else {
                val port = ipPacket.sourcePort
                proxyTcpServer.tcpSessions[port]?.takeIf {
                    it.address.hostAddress == ipPacket.destinationAddress.hostAddress
                            && it.port == ipPacket.destinationPort
                } ?: ProxySession(ipPacket.destinationAddress, ipPacket.destinationPort).also {
                    proxyTcpServer.tcpSessions[port] = it
                }
                val sourceAddr = ipPacket.sourceAddress
                ipPacket.sourceAddress = ipPacket.destinationAddress
                ipPacket.destinationAddress = sourceAddr
                ipPacket.destinationPort = proxyTcpServer.serverPort
                tunOutput.write(ipPacket.encodePacket().array())
            }
            true
        } else if (ipPacket.isUdp()){
            true
        } else {
            false
        }
    }

    /**
     * 接收到ipv6数据
     */
    private fun receiveIpV6(data: ByteArray): Boolean{
//        Log.d(tag, "ip data: ${data.toHexString()}")
        return false
    }
}