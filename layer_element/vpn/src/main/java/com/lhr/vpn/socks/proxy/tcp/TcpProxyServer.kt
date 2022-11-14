package com.lhr.vpn.socks.proxy.tcp

import android.net.VpnService
import android.system.OsConstants
import android.util.Log
import com.lhr.vpn.LocalVpnService
import com.lhr.vpn.socks.Tunnel
import com.lhr.vpn.socks.bind
import com.lhr.vpn.socks.proxy.ProxySession
import com.lhr.vpn.socks.utils.UidUtils
import com.lhr.vpn.toNetInt
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

/**
 * @author lhr
 * @date 31/10/2022
 * @des tcp 代理服务器
 */
class TcpProxyServer(private val vpnService: VpnService, val scope: CoroutineScope = MainScope()) {
    private val TAG = this::class.java.simpleName
    private val serverSocketChannel = ServerSocketChannel.open()
    private val selector = Selector.open()
    private var workJob: Job? = null

    val serverPort: Short
    val tcpSessions by lazy { mutableMapOf<Short, ProxySession>() }

    init {
        serverSocketChannel.run {
            configureBlocking(false)
            socket().bind(InetSocketAddress(0))
            register(selector, SelectionKey.OP_ACCEPT)
        }
        serverPort = serverSocketChannel.socket().localPort.toShort()
    }

    fun startProxy(){
        workJob = scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                while (isActive){
                    selector.select()
                    val iterator = selector.selectedKeys().iterator()
                    while (iterator.hasNext()){
                        val key = iterator.next()
                        when{
                            key.isAcceptable -> {
                                Log.d(TAG, "isAcceptable")
                                onAccept()
                            }
                            key.isConnectable -> {
                                Log.d(TAG, "isConnectable")
                                onConnect(key)
                            }
                        }
                        iterator.remove()
                    }
                }
            }.onFailure {
                Log.e(TAG, "work job stop", it)
            }
            Log.e(TAG, "work job end")
        }
    }

    private fun onAccept(){
        val socketChannel = serverSocketChannel.accept()
        Log.d(TAG, "socket channel ${socketChannel.socket().inetAddress}")
        val session = tcpSessions[socketChannel.socket().port.toShort()]
            ?: throw RuntimeException("no tcp proxy session")
        val localChannel = Tunnel(socketChannel.apply {
            this.configureBlocking(false)
        })
        val remoteChannel = Tunnel(SocketChannel.open().apply {
            this.configureBlocking(false)
        })
        localChannel.bind(remoteChannel)
        assert(vpnService.protect(remoteChannel.channel.socket()))
        remoteChannel.channel.register(selector, SelectionKey.OP_CONNECT, remoteChannel)
        remoteChannel.channel.connect(InetSocketAddress(socketChannel.socket().inetAddress, session.port.toNetInt()))
    }

    private fun onConnect(selectionKey: SelectionKey){
        val remoteChannel = selectionKey.attachment() as Tunnel
        assert(remoteChannel.channel.finishConnect())
        LocalVpnService.vpnService.get()?.let {
            scope.launch(IO){
                val remoteSocket = remoteChannel.channel.socket()
                val localSocket = remoteChannel.bindChannel?.channel?.socket() ?: return@launch
                val uid = UidUtils.getUidByNetLink(it,
                    OsConstants.IPPROTO_IP,
                    OsConstants.IPPROTO_TCP,
                    remoteSocket.inetAddress.hostAddress,
                    remoteSocket.port,
                    localSocket.inetAddress.hostAddress,
                    localSocket.port)
                Log.e(TAG, "session is uid $uid")
            }
        }
        TcpConnection(remoteChannel, scope).startWork()
        selectionKey.cancel()
    }

    fun stopProxy(){
        workJob?.cancel()
        selector.use {  }
        serverSocketChannel.use {  }
    }
}