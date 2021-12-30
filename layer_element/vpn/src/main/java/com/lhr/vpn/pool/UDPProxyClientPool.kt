package com.lhr.vpn.pool

import android.net.VpnService
import com.lhr.vpn.handle.IProxyTun
import com.lhr.vpn.protocol.UDPPacket
import com.lhr.vpn.proxy.ProxyConfig
import com.lhr.vpn.proxy.UDPProxyClient
import java.lang.RuntimeException
import java.net.DatagramSocket

/**
 * @author lhr
 * @date 2021/12/17
 * @des udp代理池
 */
object UDPProxyClientPool {
    private val tableClient = HashMap<Int, UDPProxyClient>()
    private val proxySocketPort = HashSet<Int>()

    fun sendPacket(vpnService: VpnService, handleTun: IProxyTun, packet: UDPPacket){
        val sourcePort = packet.getSourcePort()
        if (proxySocketPort.contains(sourcePort)){
            throw RuntimeException("udp socket 出现环路")
        }

        if (tableClient[sourcePort] == null){
            val datagramSocket = DatagramSocket()
            vpnService.protect(datagramSocket)
            proxySocketPort.add(datagramSocket.localPort)
            val proxyConfig = ProxyConfig(
                packet.getSourceAddress(),
                packet.getSourcePort(),
                packet.getTargetAddress(),
                packet.getTargetPort())
            tableClient[sourcePort] = UDPProxyClient(handleTun, datagramSocket, proxyConfig)
        }
        tableClient[sourcePort]?.pushPacket(packet)
    }
}