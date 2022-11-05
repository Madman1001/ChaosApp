package com.lhr.vpn.socks.proxy.dns

import android.net.VpnService
import com.lhr.vpn.socks.TunSocks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

/**
 * @author lhr
 * @date 5/11/2022
 * @des dns proxy server
 */
class DnsProxyServer(
    private val vpnService: VpnService,
    private val tunSocks: TunSocks,
    val scope: CoroutineScope = MainScope()) {
    private val TAG = this::class.java.simpleName
    private val selector = Selector.open()
    private var workJob: Job? = null

    private val localDnsChannel = DatagramChannel.open().apply{
        configureBlocking(false)
        socket().bind(InetSocketAddress(0))
        register(selector, SelectionKey.OP_READ)

    }

    fun startProxy(){

    }
}