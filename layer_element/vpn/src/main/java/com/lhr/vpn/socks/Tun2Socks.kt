package com.lhr.vpn.socks

import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.lhr.vpn.channel.StreamChannel
import com.lhr.vpn.socks.net.v4.NetIpPacket

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: Tun2Socks 门面类，负责ip包传递
 */
class Tun2Socks(
    private val tunInterface: ParcelFileDescriptor,
    private val vpnService: VpnService
) {
    private val tag = this::class.java.simpleName

    private val appTun by lazy { DeviceTun(tunInterface) }

    private val udpSocks by lazy { UdpSocks(vpnService, this) }
    private val tcpSocks by lazy { TcpSocks(vpnService, this) }

    private val tunChannel = object : StreamChannel<ByteArray>() {
        override fun writeData(o: ByteArray) {
            this@Tun2Socks.writeDataToTun(o)
        }

        override fun readData() {
            this@Tun2Socks.receiveDataByTun()
        }
    }

    @Synchronized
    fun startProxy() { //开始数据代理
        tunChannel.openChannel()
    }

    @Synchronized
    fun stopProxy() { //停止数据代理
        tunChannel.closeChannel()
        tunInterface.close()
        udpSocks.closeSocks()
        tcpSocks.closeSocks()
    }

    fun sendData(ipPacket: NetIpPacket) {
        synchronized(tunChannel) {
            tunChannel.sendData(ipPacket.encodePacket().array())
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
            if (ipPacket.data.isEmpty()) {
                return
            }
            //传递ip数据包
            if (ipPacket.isTcp()) {
                tcpSocks.tunToSocks(ipPacket)
            } else if (ipPacket.isUdp()) {
                udpSocks.tunToSocks(ipPacket)
            }
        }
    }

    /**
     * 写入数据到虚拟网卡中
     */
    private fun writeDataToTun(data: ByteArray) {
        if (data.isEmpty()) return
        kotlin.runCatching {
            appTun.writeTun(data)
        }
    }
}