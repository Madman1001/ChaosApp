package com.lhr.vpn.socks.proxy.udp

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.LocalVpnConfig
import com.lhr.vpn.LocalVpnConfig.Companion.HostIp
import com.lhr.vpn.socks.TunSocks
import com.lhr.vpn.socks.net.IP_VERSION_4
import com.lhr.vpn.socks.net.PROTO_UDP
import com.lhr.vpn.socks.net.v4.NetIPHeader
import com.lhr.vpn.socks.net.v4.NetPacket
import com.lhr.vpn.socks.net.v4.NetUdpHeader
import com.lhr.vpn.toIpInt
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.Pipe
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayDeque
import kotlin.random.Random

/**
 * @author lhr
 * @date 31/10/2022
 * @des udp 代理服务器
 */
class UdpProxyServer(
    private val vpnService: VpnService,
    private val tunSocks: TunSocks,
    val scope: CoroutineScope = MainScope()) {
    private val TAG = this::class.java.simpleName
    private val selector = Selector.open()
    private var workJob: Job? = null

    //本地ip地址映射
    private val localPortTable by lazy { mutableMapOf<Short, DatagramChannel>() }
    private val useTimeTable by lazy { mutableMapOf<Short, Long>() }

    private val receiveBuffer = ByteBuffer.allocate(1500)

    private var pipe = Pipe.open().apply {
        source().apply {
            configureBlocking(false)
            register(selector, SelectionKey.OP_READ)
        }
    }
    private val registerBuffer = ByteBuffer.allocate(4)
    private val sendQueue = ConcurrentLinkedQueue<DatagramPacket>()

    fun sendData(port: Short, datagramPacket: DatagramPacket){
        sendQueue.offer(datagramPacket)

        val bb = ByteBuffer.allocate(2)
        bb.asShortBuffer().put(port)
        pipe.sink().write(bb)
    }

    fun registerProxy(port: Short, channel: DatagramChannel) {
        localPortTable[port]?.close()
        localPortTable[port] = channel
        useTimeTable[port] = System.currentTimeMillis()
        Log.d(TAG, "isRegister $port = ${channel.socket().localPort}")
    }

    fun startProxy(){
        workJob = scope.launch(Dispatchers.IO){
            kotlin.runCatching {
                while (isActive){
                    selector.select()
                    val ite = selector.selectedKeys().iterator()
                    while (ite.hasNext()){
                        val key = ite.next()
                        when {
                            key.isReadable ->{
                                if (key.channel() === pipe.source()){
                                    onWrite(key)
                                } else {
                                    onRead(key)
                                }
                            }
                        }
                        ite.remove()
                    }

                    val keysIte = selector.keys().iterator()
                    while (keysIte.hasNext()){
                        val key = keysIte.next()
                        if (key.channel() is DatagramChannel){
                            onSoTimeout(key)
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
            for (entry in localPortTable) {
                entry.value.use {  }
            }
            selector.use {  }
            localPortTable.clear()
        }
    }

    fun stopProxy(){
        workJob?.cancel()
    }

    private fun onWrite(key: SelectionKey){
        registerBuffer.clear()
        val len = pipe.source().read(registerBuffer)
        if (len != Short.SIZE_BYTES) return
        registerBuffer.flip()
        val port = registerBuffer.asShortBuffer().get(0)

        if (sendQueue.isEmpty()) return

        val packet = sendQueue.poll() ?: return

        val channel = localPortTable[port] ?: DatagramChannel.open().apply {
            socket().soTimeout = 5 * 1000
            configureBlocking(false)
            socket().bind(InetSocketAddress(0))
            vpnService.protect(socket())
            register(selector, SelectionKey.OP_READ, port)
            registerProxy(port, this)
        }

        useTimeTable[port] = System.currentTimeMillis()

        channel.send(ByteBuffer.wrap(packet.data), packet.socketAddress)
        Log.d(TAG, "SEND ${String(packet.data)} TO ${packet.address.hostAddress}:${packet.port}")
    }

    private fun onRead(key: SelectionKey){
        val port = key.attachment() as Short
        val channel = localPortTable[port]
        if (channel == null){
            //没有注册对于的数据
            key.channel().close()
            key.cancel()
        }
        receiveBuffer.clear()
        val remoteAddress = channel?.receive(receiveBuffer) as InetSocketAddress
        receiveBuffer.flip()
        Log.d(TAG, "RECEIVE ${String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.limit() - receiveBuffer.position())}")
        val dataLen = receiveBuffer.limit() - receiveBuffer.position()
        val packet = NetPacket().apply {
            ipHeader.run {
                version = IP_VERSION_4
                timeToLive = 64
                identification = Random(System.currentTimeMillis()).nextInt().toShort()
                sourceIp = remoteAddress.hostString.toIpInt()
                destinationIp = HostIp
                upperProtocol = PROTO_UDP.toByte()
                flagAndOffsetFrag = 0x4000
                totalLength = (20 + 8 + dataLen).toShort()
                headerLength = 5
                Log.d(TAG, "send Ip $this")
            }
            udpHeader.run {
                sourcePort = remoteAddress.port.toShort()
                destinationPort = port
                totalLength = (dataLen + 8).toShort()
                Log.d(TAG, "send Udp $this")
            }
            data = ByteArray(dataLen).apply {
                receiveBuffer.get(this)
            }
        }

        useTimeTable[port] = System.currentTimeMillis()
        tunSocks.sendTunData(packet.encodePacket())
    }

    private fun onSoTimeout(key: SelectionKey) {
        val port = key.attachment() as Short
        val lastUseTime = System.currentTimeMillis() - (useTimeTable[port] ?: System.currentTimeMillis())
        val channel = localPortTable[port]
        if (channel == null){
            useTimeTable[port] = 0
            key.cancel()
            return
        }
        if (channel.socket().soTimeout <= lastUseTime){
            channel.close()
            localPortTable.remove(port)
            useTimeTable.remove(port)
            key.cancel()
            Log.e(TAG, "datagram channel is over")
        }
    }
}