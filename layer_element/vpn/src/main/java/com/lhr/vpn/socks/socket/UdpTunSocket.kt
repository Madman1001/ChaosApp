package com.lhr.vpn.socks.socket

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.channel.StreamChannel
import com.lhr.vpn.ext.toHexString
import com.lhr.vpn.ext.isInsideToOutside
import com.lhr.vpn.ext.isOutsideToInside
import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.SessionTable
import com.lhr.vpn.socks.Tun2Socks
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class UdpTunSocket(
    val bean: ProxyRouteSession,
    val vpnService: VpnService,
    private val tunSocks: Tun2Socks,
): ITunSocket{
    private val tag = this::class.java.simpleName

    //remote proxy socket
    private val remoteSocket = DatagramSocket().apply { vpnService.protect(this) }
    @Volatile
    private var remoteChannel: StreamChannel<ByteArray>? = null
    @Volatile
    private var remoteConnectJob: Job? = null

    //local proxy socket
    private val localServerSocket = DatagramSocket()
    @Volatile
    private var localChannel: StreamChannel<ByteArray>? = null
    @Volatile
    private var localConnectJob: Job? = null

    override fun handlePacket(packet: NetIpPacket){
        if (bean.state == ProxyRouteSession.STATE_INVALID) return

        RunPool.execute(TunRunnable("$tag$this-out"){
            val udpPacket = NetUdpPacket(packet.data)
            if (packet.isInsideToOutside()){
                //inside to outside
                val packetSourcePort = udpPacket.sourcePort.toUShort().toInt()
                val packetTargetPort = udpPacket.targetPort.toUShort().toInt()
                val sourceSocketPort = bean.sourcePort
                val localSocketPort = localServerSocket.localPort
                if (packetSourcePort == localSocketPort){
                    forwardToAppSocket(packet, udpPacket)
                } else if (packetSourcePort == sourceSocketPort){
                    forwardToLocalSocket(packet, udpPacket)
                }
            } else if (packet.isOutsideToInside()){
                //outside to inside
            }
        })
        if (localChannel?.isOpened != true && localConnectJob?.isActive != true){
            startLocalJob()
        }
        if (remoteChannel?.isOpened != true && remoteConnectJob?.isActive != true){
            startRemoteJob()
        }
    }

    /**
     * 通过虚拟网卡转发到app socket
     */
    private fun forwardToAppSocket(packet: NetIpPacket, udpPacket: NetUdpPacket){
        udpPacket.targetPort = bean.sourcePort.toShort()
        udpPacket.sourcePort = bean.targetPort.toShort()
        packet.sourceAddress = bean.targetAddress
        packet.targetAddress = bean.sourceAddress
        packet.data = udpPacket.encodePacket().array()
        Log.d(tag, "forwardToAppSocket $packet $udpPacket")
        tunSocks.sendData(packet)
    }

    /**
     * 通过虚拟网卡转发到local socket
     */
    private fun forwardToLocalSocket(packet: NetIpPacket, udpPacket: NetUdpPacket){
        //set local port
        udpPacket.targetPort = localServerSocket.localPort.toShort()
        udpPacket.sourcePort = bean.targetPort.toShort()
        packet.sourceAddress = bean.targetAddress
        packet.targetAddress = bean.sourceAddress
        packet.data = udpPacket.encodePacket().array()
        Log.d(tag, "forwardToLocalSocket $packet $udpPacket")
        tunSocks.sendData(packet)
    }

    /**
     * 启动Local接收线程
     */
    private fun startLocalJob(){
        localChannel?.closeChannel()
        localChannel = null

        val inputRunnable = TunRunnable("$tag$this-local_in"){
            val localReceivePacket = DatagramPacket(ByteArray(1024), 1024)
            localChannel = object : StreamChannel<ByteArray>(){
                override fun writeData(o: ByteArray) {

                    //将数据发送到内部
                    val target = bean.targetAddress
                    val targetPort = bean.targetPort
                    val address = InetSocketAddress(target, targetPort)
                    val datagramPacket = DatagramPacket(o, o.size, address)
                    Log.e(tag, "localSocket send $target $targetPort ${o.toHexString()}")
                    localServerSocket.send(datagramPacket)
                }

                override fun readData() {
                    //接收内部数据
                    localServerSocket.receive(localReceivePacket)
                    if (localReceivePacket.length > 0){
                        val data = ByteArray(localReceivePacket.length)
                        System.arraycopy(localReceivePacket.data, 0, data, 0, data.size)
                        Log.e(tag, "localSocket receive ${data.toHexString()}")
                        remoteChannel?.sendData(data)
                    } else {
                        Thread.sleep(100)
                    }
                }
            }
            localChannel?.openChannel()
        }
        localConnectJob = GlobalScope.launch(Dispatchers.IO){
            inputRunnable.run()
        }
    }

    /**
     * 启动Remote接收线程
     */
    private fun startRemoteJob(){
        remoteChannel?.closeChannel()
        remoteChannel = null

        val inputRunnable = TunRunnable("$tag$this-remote_in"){
            val remoteReceivePacket = DatagramPacket(ByteArray(1024), 1024)
            remoteChannel = object : StreamChannel<ByteArray>(){
                override fun writeData(o: ByteArray) {

                    //将数据发送到外部
                    val target = bean.targetAddress
                    val targetPort = bean.targetPort
                    val address = InetSocketAddress(target, targetPort)
                    val datagramPacket = DatagramPacket(o, o.size, address)
                    Log.e(tag, "remoteSocket send $target $targetPort ${o.toHexString()}")
                    remoteSocket.send(datagramPacket)
                }

                override fun readData() {

                    //接收外部数据
                    remoteSocket.receive(remoteReceivePacket)
                    if (remoteReceivePacket.length > 0){
                        val data = ByteArray(remoteReceivePacket.length)
                        System.arraycopy(remoteReceivePacket.data, 0, data, 0, data.size)
                        Log.e(tag, "remoteSocket receive ${data.toHexString()}")
                        localChannel?.sendData(data)
                    } else {
                        Thread.sleep(100)
                    }
                }
            }
            remoteChannel?.openChannel()
        }
        remoteConnectJob = GlobalScope.launch(Dispatchers.IO){
            inputRunnable.run()
        }
    }

    override fun createLocalKey(): String{
        return  SessionTable.createSessionKey(
            bean.sourceAddress,
            localServerSocket.localPort,
            bean.targetAddress,
            bean.targetPort
        )
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("\n").append("source port ").append(bean.sourcePort)
        sb.append("\n").append("target port ").append(bean.targetPort)
        sb.append("\n").append("local socket port ").append(localServerSocket.localPort)
        sb.append("\n").append("remote socket port ").append(remoteSocket.localPort)
        return sb.toString()
    }

    override fun close() {
        kotlin.runCatching {
            localConnectJob?.cancel()
            localConnectJob = null
            localChannel?.closeChannel()
            localChannel = null
        }

        kotlin.runCatching {
            remoteConnectJob?.cancel()
            remoteConnectJob = null
            remoteChannel?.closeChannel()
            remoteChannel = null
        }

        kotlin.runCatching {
            remoteSocket.close()
        }

        kotlin.runCatching {
            localServerSocket.close()
        }

        Log.e(tag, "close udp socket ${localServerSocket.localPort} ${remoteSocket.localPort}")
    }
}