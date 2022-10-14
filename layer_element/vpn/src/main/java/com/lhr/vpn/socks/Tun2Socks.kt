package com.lhr.vpn.socks

import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.lhr.vpn.channel.StreamChannel
import com.lhr.vpn.socks.net.v4.NetIpPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.Socket

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: Tun2Socks
 */
class Tun2Socks(
    private val tunInterface: ParcelFileDescriptor,
    private val vpnService: VpnService
){
    private val tag = this::class.java.simpleName

    private val appTun by lazy { NetTun(tunInterface) }

    //tcp socket 注册表
    private val tcpTunSocketMap by lazy { mutableMapOf<String, TcpTunSocket>() }

    //udp socket 注册表
    private val udpTunSocketMap by lazy { mutableMapOf<String, UdpTunSocket>() }

    private val tunChannel = object : StreamChannel<ByteArray>(){
        override fun writeData(o: ByteArray) {
            this@Tun2Socks.writeDataToTun(o)
        }

        override fun readData() {
            this@Tun2Socks.receiveDataByTun()
        }
    }

    @Synchronized
    fun startProxy(){ //开始数据代理
        tunChannel.openChannel()
    }

    @Synchronized
    fun stopProxy(){ //停止数据代理
        tunChannel.closeChannel()
    }

    fun sendData(data: ByteArray){
        synchronized(tunChannel){
            tunChannel.sendData(data)
        }
    }

    /**
     * 读取数据然后进行分发
     */
    private fun receiveDataByTun() {
        val data = appTun.readTun()
        if (data.isEmpty()) return

        kotlin.runCatching {
            val headerLength = (data[0].toUByte().toInt() and 0x0f)
            //非ip v4 包
            if (headerLength <= 0) {
                return
            }
            val ipPacket = NetIpPacket(data)
            if (ipPacket.data.isEmpty()){
                return
            }
            //todo 传递ip数据包
            if (ipPacket.isTcp()){
                val sock = obtainTcpTunSocket(ipPacket)
                sock.sendTun2Socket(ipPacket)
            } else if (ipPacket.isUdp()){
                val sock = obtainUdpTunSocket(ipPacket)
                sock.sendTun2Socket(ipPacket)
            }
        }
    }

    /**
     * 写入数据到虚拟网卡中
     */
    private fun writeDataToTun(data: ByteArray){
        if (data.isEmpty()) return
        kotlin.runCatching {
            appTun.writeTun(data)
        }
    }

    private fun obtainTcpTunSocket(ipPacket: NetIpPacket): TcpTunSocket{
        val source = ipPacket.ipHeader.source_ip_address
        val target = ipPacket.ipHeader.target_ip_address
        val sourcePort =
            ipPacket.data[0].toUByte().toInt() shl 8 or ipPacket.data[1].toUByte().toInt()
        val targetPort =
            ipPacket.data[2].toUByte().toInt() shl 8 or ipPacket.data[3].toUByte().toInt()

        val key = createMapKey(source, sourcePort, target, targetPort)
        var sock = tcpTunSocketMap[key]
        if (sock == null){
            //创建udp socket
            val socket = Socket()
            socket.bind(null)
            vpnService.protect(socket)
            val bean = NetProxyBean(source,sourcePort, target, targetPort)
            sock = TcpTunSocket(bean, this, socket)
            tcpTunSocketMap[key] = sock
        }
        return sock
    }

    private fun obtainUdpTunSocket(ipPacket: NetIpPacket): UdpTunSocket{
        val source = ipPacket.ipHeader.source_ip_address
        val target = ipPacket.ipHeader.target_ip_address
        val sourcePort =
            ipPacket.data[0].toUByte().toInt() shl 8 or ipPacket.data[1].toUByte().toInt()
        val targetPort =
            ipPacket.data[2].toUByte().toInt() shl 8 or ipPacket.data[3].toUByte().toInt()

        val key = createMapKey(source, sourcePort, source, sourcePort)
        var sock = udpTunSocketMap[key]
        if (sock == null){
            //创建udp socket
            val bean = NetProxyBean(source,sourcePort, target, targetPort)
            sock = UdpTunSocket(bean, this, DatagramSocket().apply { vpnService.protect(this) })
            udpTunSocketMap[key] = sock
        }
        return sock

    }

    private fun createMapKey(source: Inet4Address, sourcePort: Int, target: Inet4Address, targetPort: Int): String{
        return StringBuilder()
            .append(source.address).append(":").append(sourcePort)
            .append("-")
            .append(target.address).append(":").append(targetPort)
            .toString()
    }
}