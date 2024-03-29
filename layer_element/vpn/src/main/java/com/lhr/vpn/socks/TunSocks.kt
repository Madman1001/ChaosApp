package com.lhr.vpn.socks

import android.system.OsConstants
import android.util.Log
import com.lhr.vpn.*
import com.lhr.vpn.socks.net.IP_VERSION_4
import com.lhr.vpn.socks.net.IP_VERSION_6
import com.lhr.vpn.socks.net.v4.NetPacket
import com.lhr.vpn.socks.proxy.ProxySession
import com.lhr.vpn.socks.proxy.Type
import com.lhr.vpn.socks.proxy.tcp.TcpProxyServer
import com.lhr.vpn.socks.proxy.udp.UdpProxyServer
import com.lhr.vpn.socks.utils.UidUtils
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: Tun2Socks 负责ip包传递
 */
class TunSocks(
    private val vpnService: LocalVpnService
) {
    private val tag = this::class.java.simpleName

    private val socksScope = CoroutineScope(Dispatchers.IO + Job())

    private val proxyTcpServer by lazy { TcpProxyServer(vpnService, socksScope) }

    private val proxyUdpServer by lazy { UdpProxyServer(vpnService, this, socksScope) }

    private val networkToDeviceQueue by lazy { ConcurrentLinkedQueue<ByteBuffer>() }

    private val hostIp = LocalVpnConfig.PROXY_ADDRESS.toIpInt()

    @Volatile
    private var workJob: Job? = null

    fun startProxy() { //开始数据代理
        startWorkJob()
        proxyTcpServer.startProxy()
        proxyUdpServer.startProxy()
    }

    fun stopProxy() { //停止数据代理
        socksScope.cancel()
        proxyTcpServer.stopProxy()
        proxyUdpServer.stopProxy()
        vpnService.tunInterface.close()
    }

    fun sendTunData(byteArray: ByteArray, offset: Int = 0, len: Int = byteArray.size) {
        networkToDeviceQueue.offer(ByteBuffer.wrap(byteArray, offset, len))
    }

    private fun startWorkJob() {
        if (workJob?.isActive == true) return
        workJob = socksScope.launch(Dispatchers.IO) {
            var tunOutput: FileChannel? = null
            var tunInput: FileChannel? = null
            kotlin.runCatching {
                val buffer = ByteBuffer.allocate(LocalVpnService.config.mtu)
                val packet = NetPacket()
                tunOutput = FileOutputStream(vpnService.tunInterface.fileDescriptor).channel
                tunInput = FileInputStream(vpnService.tunInterface.fileDescriptor).channel

                var sendData = false
                var receiveData = false
                while (isActive) {
                    buffer.clear()
                    val len = tunInput?.read(buffer) ?: 0
                    if (len > 0) {
                        Log.d(tag, "read:${len}byte\n")
                        buffer.flip()
                        packet.decodePacket(
                            buffer.array(),
                            buffer.position(),
                            buffer.limit() - buffer.position()
                        )
                        onReceive(packet)
                        receiveData = true
                    } else {
                        receiveData = false
                    }

                    val sendBuffer = networkToDeviceQueue.poll()
                    if (sendBuffer != null) {
                        Log.d(tag, "write:${sendBuffer.limit() - sendBuffer.position()}byte\n")
                        while (sendBuffer.hasRemaining()) {
                            tunOutput?.write(sendBuffer)
                        }
                        sendData = true
                    } else {
                        sendData = false
                    }

                    if (!sendData && !receiveData) {
                        delay(100)
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
            tunInput?.use { }
            tunOutput?.use { }
        }
    }

    private fun onReceive(packet: NetPacket){
        when(packet.ipHeader.version){
            IP_VERSION_4 -> receiveIpV4(packet)
            IP_VERSION_6 -> receiveIpV6(packet)
            else -> {
                Log.e(tag, "unknown ip  data: ${packet.ipHeader}")
            }
        }
    }

    /**
     * 转发ipv4数据
     */
    private fun receiveIpV4(packet: NetPacket) {
        if (packet.ipHeader.sourceIp != hostIp) return
        assert(packet.calculateIpChecksum().toInt() == 0)
        val context = LocalVpnService.vpnService.get() ?: return
        //传递ip数据包
        if (packet.isTcp()) {
            Log.d(tag, "read tcp packet:${packet.tcpHeader}")
            assert(packet.calculateTcpChecksum().toInt() == 0)
            if (packet.tcpHeader.sourcePort == proxyTcpServer.serverPort) {
                val session = proxyTcpServer.tcpSessions[packet.tcpHeader.destinationPort] ?: return
                val sourceIp = packet.ipHeader.sourceIp
                packet.ipHeader.sourceIp = packet.ipHeader.destinationIp
                packet.tcpHeader.sourcePort = session.dport
                packet.ipHeader.destinationIp = sourceIp
                sendTunData(packet.encodePacket())
            } else {
                val port = packet.tcpHeader.sourcePort
                proxyTcpServer.tcpSessions[port]?.takeIf {
                    it.daddrString.toIpInt() == packet.ipHeader.destinationIp
                            && it.dport == packet.tcpHeader.destinationPort
                } ?: run {
                    val localSocketAddress =
                        InetSocketAddress(
                            packet.ipHeader.sourceIp.toIpString(),
                            packet.tcpHeader.sourcePort.toNetInt()
                        )
                    val targetSocketAddress =
                        InetSocketAddress(
                            packet.ipHeader.destinationIp.toIpString(),
                            packet.tcpHeader.destinationPort.toNetInt()
                        )
                    val uid = UidUtils.getUidByNetLink(
                        context,
                        OsConstants.IPPROTO_TCP,
                        localSocketAddress,
                        targetSocketAddress
                    )
                    Log.e(this@TunSocks.tag, "tcp session is uid $uid")
                    val pm = context.packageManager
                    pm.getPackagesForUid(uid)?.forEach {
                        Log.e(this@TunSocks.tag, "uid $uid package is $it")
                    }
                    ProxySession(uid, localSocketAddress, targetSocketAddress).apply {
                        this.type = Type.TCP
                        proxyTcpServer.tcpSessions[port] = this
                    }
                }
                val sourceIp = packet.ipHeader.sourceIp
                packet.ipHeader.sourceIp = packet.ipHeader.destinationIp
                packet.ipHeader.destinationIp = sourceIp
                packet.tcpHeader.destinationPort = proxyTcpServer.serverPort
                sendTunData(packet.encodePacket())
            }
        } else if (packet.isUdp()) {
            Log.d(tag, "read udp packet:${packet.udpHeader}")
            assert(packet.calculateUdpChecksum().toInt() == 0)
            val port = packet.udpHeader.sourcePort
            val localSocketAddress =
                InetSocketAddress(
                    packet.ipHeader.sourceIp.toIpString(),
                    packet.udpHeader.sourcePort.toNetInt()
                )
            val targetSocketAddress =
                InetSocketAddress(
                    packet.ipHeader.destinationIp.toIpString(),
                    packet.udpHeader.destinationPort.toNetInt()
                )
            val uid = UidUtils.getUidByNetLink(
                context,
                OsConstants.IPPROTO_UDP,
                localSocketAddress,
                targetSocketAddress
            )

            proxyUdpServer.udpSessions[port]?.takeIf {
                it.uid == uid
            } ?: run {
                Log.e(this@TunSocks.tag, "udp session is uid $uid")
                val pm = context.packageManager
                pm.getPackagesForUid(uid)?.forEach {
                    Log.e(this@TunSocks.tag, "uid $uid package is $it")
                }
                ProxySession(uid, localSocketAddress, targetSocketAddress).apply {
                    this.type = Type.UDP
                    proxyUdpServer.udpSessions[port] = this
                }
            }

            val datagramPacket = DatagramPacket(
                packet.data,
                packet.data.size,
                InetSocketAddress(
                    packet.ipHeader.destinationIp.toIpString(),
                    packet.udpHeader.destinationPort.toNetInt()
                )
            )
            proxyUdpServer.sendData(packet.udpHeader.sourcePort, datagramPacket)
        }
    }

    /**
     * 接收到ipv6数据
     */
    private fun receiveIpV6(packet: NetPacket) {
//        Log.d(tag, "ip data: ${data.toHexString()}")
        return
    }
}