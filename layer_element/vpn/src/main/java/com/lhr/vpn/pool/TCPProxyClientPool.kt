package com.lhr.vpn.pool

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.handle.IProxyTun
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.proxy.ProxyConfig
import com.lhr.vpn.proxy.TCPProxyClient
import java.lang.RuntimeException
import java.net.Socket

/**
 * @author lhr
 * @date 2021/12/17
 * @des udp代理池
 */
object TCPProxyClientPool {
    private val tableClient = HashMap<Int, TCPProxyClient>()
    private val proxySocketPort = HashSet<Int>()

    fun sendPacket(vpnService: VpnService, handleTun: IProxyTun, packet: TCPPacket){
        val sourcePort = packet.getSourcePort()
        if (proxySocketPort.contains(sourcePort)){
            throw RuntimeException("tcp socket 出现环路")
        }

        if (tableClient[sourcePort] == null){
            val socket = Socket()
            socket.bind(null)
            vpnService.protect(socket)
            proxySocketPort.add(socket.localPort)
            val proxyConfig = ProxyConfig(
                packet.getSourceAddress(),
                packet.getSourcePort(),
                packet.getTargetAddress(),
                packet.getTargetPort())
            tableClient[sourcePort] = TCPProxyClient(handleTun, socket, proxyConfig)
        }
        tableClient[sourcePort]?.pushPacket(packet)
    }
}