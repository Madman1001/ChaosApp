package com.lhr.vpn.socks.proxy

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.LocalVpnConfig
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
import kotlin.collections.ArrayDeque
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

    //本地ip地址映射
    private val localPortTable by lazy { mutableMapOf<Short, DatagramChannel>() }
    private val receiveBuffer = ByteBuffer.allocate(1500)

    private var pipe = Pipe.open().apply {
        source().apply {
            configureBlocking(false)
            register(selector, SelectionKey.OP_READ)
        }
    }
    private val registerBuffer = ByteBuffer.allocate(4)
    private val sendQueue = ArrayDeque<DatagramPacket>()

    fun sendData(port: Short, datagramPacket: DatagramPacket){
        sendQueue.addLast(datagramPacket)

        val bb = ByteBuffer.allocate(2)
        bb.asShortBuffer().put(port)
        pipe.sink().write(bb)
    }

    fun registerProxy(port: Short, channel: DatagramChannel) {
        localPortTable[port]?.close()
        localPortTable[port] = channel
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
                        when{
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

        val packet = sendQueue.removeFirst()

        val channel = localPortTable[port] ?: DatagramChannel.open().apply {
            configureBlocking(false)
            socket().bind(InetSocketAddress(0))
            vpnService.protect(socket())
            register(selector, SelectionKey.OP_READ, port)
            registerProxy(port, this)
        }

        channel.send(ByteBuffer.wrap(packet.data), packet.socketAddress)
        Log.d(TAG, "SEND ${String(packet.data)} TO ${packet.address.hostAddress}:${packet.port}")
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
            version = IP_VERSION_4
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