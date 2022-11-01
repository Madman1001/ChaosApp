package com.lhr.vpn.socks.proxy

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.LocalVpnConfig
import com.lhr.vpn.socks.TunSocks
import com.lhr.vpn.socks.net.PROTO_UDP
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.Pipe
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import kotlin.random.Random

/**
 * @author lhr
 * @date 31/10/2022
 * @des
 */
class UdpProxyServer(
    private val vpnService: VpnService,
    private val tunSocks: TunSocks,
    val scope: CoroutineScope = MainScope()) {
    private val TAG = this::class.java.simpleName
    private val selector = Selector.open()
    private var workJob: Job? = null

    private var pipe = Pipe.open().apply {
        source().apply {
            configureBlocking(false)
            register(selector, SelectionKey.OP_READ)
        }
    }
    //本地ip地址映射
    private val localPortTable by lazy { mutableMapOf<Int, DatagramChannel>() }
    private val receiveBuffer = ByteBuffer.allocate(1500)

    private val registerBuffer = ByteBuffer.allocate(4)
    fun getChannel(port: Int): DatagramChannel? {
        return localPortTable[Integer.valueOf(port)]
    }

    fun registerProxy(port: Int): DatagramChannel {
        localPortTable[port]?.close()
        val channel = DatagramChannel.open().apply {
            configureBlocking(false)
            socket().bind(InetSocketAddress(0))

            val bb = ByteBuffer.allocate(4)
            bb.asIntBuffer().put(port)
            pipe.sink().write(bb)

            vpnService.protect(socket())
        }
        localPortTable[port] = channel
        return channel
    }

    fun startProxy(){
        workJob = scope.launch(Dispatchers.IO){
            kotlin.runCatching {
                while (isActive){
                    selector.select()
                    val ite = selector.selectedKeys().iterator()
                    while (ite.hasNext()){
                        val key = ite.next()
                        when{
                            key.isReadable ->{
                                if (key.channel() === pipe.source()){
                                    Log.d(TAG, "isRegister")
                                    onRegister(key)
                                } else {
                                    Log.d(TAG, "isReadable")
                                    onRead(key)
                                }
                            }
                        }
                        ite.remove()
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun stopProxy(){
        workJob?.cancel()
        selector.use {  }
        for (entry in localPortTable) {
            entry.value.use {  }
        }
        localPortTable.clear()
    }

    private fun onRegister(key: SelectionKey){
        registerBuffer.clear()
        val len = pipe.source().read(registerBuffer)
        if (len != 4) return
        registerBuffer.flip()
        val port = registerBuffer.asIntBuffer().get(0)
        val channel = localPortTable[port] ?: return
        channel.register(selector, SelectionKey.OP_READ, port)
    }

    private fun onRead(key: SelectionKey){
        val port = key.attachment() as Int
        val channel = localPortTable[port]
        if (channel == null){
            //没有注册对于的数据
            key.channel().close()
        }
        receiveBuffer.clear()
        val remoteAddress = channel?.receive(receiveBuffer) as InetSocketAddress
        receiveBuffer.flip()
        Log.d(TAG, "RECEIVE ${String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.limit() - receiveBuffer.position())}")
        val udpPacket = NetUdpPacket().apply {
            this.sourcePort = remoteAddress.port.toShort()
            this.targetPort = port.toShort()
            this.data = receiveBuffer.array().copyInto(ByteArray(receiveBuffer.limit() - receiveBuffer.position()), startIndex = receiveBuffer.position(), endIndex = receiveBuffer.limit())
        }
        val ipPacket = NetIpPacket().apply {
            data = udpPacket.encodePacket().array()
            flag = 2
            offsetFrag = 0
            timeToLive = 64
            identification = Random(System.currentTimeMillis()).nextInt().toShort()
            sourceAddress = remoteAddress.address
            destinationAddress = InetAddress.getByName(LocalVpnConfig.PROXY_ADDRESS)
            upperProtocol = PROTO_UDP.toByte()
        }
        tunSocks.sendTunData(ipPacket.encodePacket().array())
    }
}