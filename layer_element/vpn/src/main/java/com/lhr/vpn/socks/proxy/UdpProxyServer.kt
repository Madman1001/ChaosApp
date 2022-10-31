package com.lhr.vpn.socks.proxy

import android.net.VpnService
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

/**
 * @author lhr
 * @date 31/10/2022
 * @des
 */
class UdpProxyServer(private val vpnService: VpnService, val scope: CoroutineScope = MainScope()) {
    private val TAG = this::class.java.simpleName

    private val serverDatagramChannel = DatagramChannel.open()

    private val selector = Selector.open()
    private var workJob: Job? = null

    val serverPort: Int
    val udpSessions by lazy { mutableMapOf<Int, ProxySession>() }

    init {
        serverDatagramChannel.run {
            configureBlocking(false)
            socket().bind(InetSocketAddress(0))
            register(selector, SelectionKey.OP_WRITE or SelectionKey.OP_READ)
        }
        serverPort = serverDatagramChannel.socket().localPort
    }

    fun startProxy(){

    }

    fun stopProxy(){
        workJob?.cancel()
        selector.use {  }
        serverDatagramChannel.use {  }
    }
}