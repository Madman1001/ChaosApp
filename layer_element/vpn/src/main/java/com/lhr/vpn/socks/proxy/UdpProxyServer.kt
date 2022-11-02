package com.lhr.vpn.socks.proxy

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.LocalVpnConfig
import com.lhr.vpn.socks.TunSocks
import com.lhr.vpn.socks.net.PROTO_UDP
import com.lhr.vpn.socks.net.v4.NetIPHeader
import com.lhr.vpn.socks.net.v4.NetPacket
import com.lhr.vpn.socks.net.v4.NetUdpHeader
import com.lhr.vpn.toIpInt
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
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
    private val hostIp = LocalVpnConfig.PROXY_ADDRESS.toIpInt()

    private var pipe = Pipe.open().apply {
        source().apply {
            configureBlocking(false)
            register(selector, SelectionKey.OP_READ)
        }
    }
    //本地ip地址映射
    private val localPortTable by lazy { mutableMapOf<Short, DatagramChannel>() }
    private val receiveBuffer = ByteBuffer.allocate(1500)

    private val registerBuffer = ByteBuffer.allocate(4)
    fun getChannel(port: Short): DatagramChannel? {
        return localPortTable[port]
    }

    fun registerProxy(port: Short): DatagramChannel {
        localPortTable[port]?.close()
        val channel = DatagramChannel.open().apply {
            configureBlocking(false)
            socket().bind(InetSocketAddress(0))

            val bb = ByteBuffer.allocate(2)
            bb.asShortBuffer().put(port)
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
        val port = registerBuffer.asShortBuffer().get(0)
        val channel = localPortTable[port] ?: return
        channel.register(selector, SelectionKey.OP_READ, port)
    }

    private fun onRead(key: SelectionKey){
        val port = key.attachment() as Short
        val channel = localPortTable[port]
        if (channel == null){
            //没有注册对于的数据
            key.channel().close()
        }
        receiveBuffer.clear()
        val remoteAddress = channel?.receive(receiveBuffer) as InetSocketAddress
        receiveBuffer.flip()
        Log.d(TAG, "RECEIVE ${String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.limit() - receiveBuffer.position())}")
        val dataLen = receiveBuffer.limit() - receiveBuffer.position()
        val packetData = ByteArray(20 + 8 + dataLen)
        NetIPHeader(packetData).run{
            timeToLive = 64
            identification = Random(System.currentTimeMillis()).nextInt().toShort()
            sourceIp = remoteAddress.hostString.toIpInt()
            destinationIp = hostIp
            upperProtocol = PROTO_UDP.toByte()
            flagAndOffsetFrag = 0x4000
            totalLength = packetData.size.toShort()
            headerLength = 5
            Log.d(TAG, "send Ip $this")
        }
        NetUdpHeader(packetData, 20).run {
            sourcePort = remoteAddress.port.toShort()
            destinationPort = port
            totalLength = (dataLen + 8).toShort()
            Log.d(TAG, "send Udp $this")
        }
        System.arraycopy(receiveBuffer.array(), receiveBuffer.position(), packetData, 28, dataLen)
        NetPacket(packetData).setChecksum()

        tunSocks.sendTunData(packetData)
    }
}