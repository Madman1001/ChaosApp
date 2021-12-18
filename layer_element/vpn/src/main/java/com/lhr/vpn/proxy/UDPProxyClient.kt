 package com.lhr.vpn.proxy

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.constant.LocalVpnConfig
import com.lhr.vpn.handle.IProxyTun
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.IProtocol
import com.lhr.vpn.protocol.UDPPacket
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*
import kotlin.random.Random

 /**
 * @author lhr
 * @date 2021/12/4
 * @des udp 客户端代理类
 */
class UDPProxyClient(vpnService: VpnService,
                     private val handleTun: IProxyTun,
                     private val liveTime: Long = Long.MAX_VALUE): IProxyBind{
    private val tag = "UDPProxyClient"
    private val packetList = Vector<UDPPacket>()
    private val udpSocket = DatagramSocket()
    private var currentUdpSendThread: Thread? = null
    private var currentUdpReceiveThread: Thread? = null

    @Volatile
    private var bindProxyPort = 0
    @Volatile
    private var isStart = false

    init {
        vpnService.protect(udpSocket)
    }

    override fun getStatus(): IProxyBind.BindStatus {
        return if (bindProxyPort == 0){
            IProxyBind.BindStatus.UNBOUND
        }else{
            IProxyBind.BindStatus.BOUND
        }
    }

    @Synchronized
    override fun bind(port: Int) {
        if (!isStart){
            isStart = true
            Thread({
                sendRun()
            }, tag).start()
            Thread({
                receiveRun()
            }, tag).start()
        }
        bindProxyPort = port
    }

    @Synchronized
    override fun unbind() {
        bindProxyPort = 0
    }

    fun release(){
        if (currentUdpSendThread != null){
            isStart = false
            if (currentUdpSendThread?.state == Thread.State.TIMED_WAITING){
                currentUdpSendThread?.interrupt()
            }
        }
    }

    fun sendPacket(packet: UDPPacket){
        packetList.add(packet)
        if (currentUdpSendThread?.state == Thread.State.TIMED_WAITING){
            currentUdpSendThread?.interrupt()
        }
    }

    private fun sendRun() {
        currentUdpSendThread = Thread.currentThread()
        while (isStart){
            if (packetList.isNotEmpty()){
                Log.d(tag, "start proxy udp send")
                val packet = packetList.removeFirst()
                udpSocket.send(packet2DatagramPacket(packet))
                Log.d(tag, "end proxy udp send")
            }else{
                try {
                    Thread.sleep(liveTime)
                    break
                }catch (e: InterruptedException){
                }
                Log.e(tag,"线程被唤醒")
            }
        }
    }

    private fun receiveRun() {
        currentUdpReceiveThread = Thread.currentThread()
        while (isStart){
            val data = ByteArray(1024)
            val receivePacket = DatagramPacket(data, data.size)
            try {
                Log.d(tag, "start proxy udp receive")
                udpSocket.receive(receivePacket)
                val str = String(receivePacket.data, 0, receivePacket.length)
                handleTun.outputData(datagramPacket2Packet(receivePacket))
                Log.d(tag, "end proxy udp receive:$str")
            }catch (e: Exception){
            }
        }
    }

    private fun packet2DatagramPacket(packet: UDPPacket): DatagramPacket {
        Log.e(tag,"UDPPacket hostname ${packet.getTargetAddress()} port ${packet.getTargetPort()}")
        val buf = packet.getData()
        val address = InetSocketAddress(packet.getTargetAddress(), packet.getTargetPort())
        val udpPacket = DatagramPacket(buf, buf.size)
        udpPacket.socketAddress = address
        return udpPacket
    }

    private fun datagramPacket2Packet(datagramPacket: DatagramPacket): IProtocol {
        Log.e(tag,"DatagramPacket hostname ${datagramPacket.address.hostAddress} port ${datagramPacket.port}")
        val packet = UDPPacket()
        packet.setData(datagramPacket.data,datagramPacket.offset,datagramPacket.length)
        packet.setTargetPort(bindProxyPort)
        packet.setTargetAddress(LocalVpnConfig.PROXY_ADDRESS)
        packet.setSourcePort(datagramPacket.port)
        packet.setSourceAddress(datagramPacket.address.hostAddress)
        packet.setOffsetFrag(0)
        packet.setFlag(2)
        packet.setTimeToLive(64)
        packet.setIdentification(Random(System.currentTimeMillis()).nextInt().toShort())
        return packet
    }
}