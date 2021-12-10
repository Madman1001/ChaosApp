package com.lhr.vpn.pool

import android.net.VpnService
import android.util.LruCache
import com.lhr.vpn.handle.IProxyTun
import com.lhr.vpn.protocol.UDPPacket
import com.lhr.vpn.proxy.UDPProxyClient

/**
 * @author lhr
 * @date 2021/12/17
 * @des udp代理池
 */
object UDPProxyClientPool {
    private val tableClient = HashMap<Int, UDPProxyClient>()

    fun sendPacket(vpnService: VpnService, handleTun: IProxyTun, packet: UDPPacket){
        val sourcePort = packet.getSourcePort()
        if (tableClient[sourcePort] == null){
            tableClient[sourcePort] = UDPProxyClient(vpnService, handleTun).apply {
                this.bind(sourcePort)
            }
        }
        tableClient[sourcePort]?.sendPacket(packet)
    }
}