package com.lhr.vpn.proxy

import android.util.Log
import com.lhr.vpn.constant.LocalVpnConfig
import com.lhr.vpn.handle.IProxyTun
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.util.ByteLog
import java.lang.RuntimeException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import kotlin.random.Random

/**
 * @author lhr
 * @date 2021/12/4
 * @des udp 客户端代理类
 */
class TCPProxyClient(
    private val handleTun: IProxyTun,
    private val tcpSocket: Socket,
    private val liveTime: Long = Long.MAX_VALUE
) : IProxyBind {
    private val tag = "TCPProxyClient"
    private val packetList = Vector<TCPPacket>()

    private var currentTcpSendThread: Thread? = null
    private var currentTcpReceiveThread: Thread? = null

    private var serverSerialNumber = 0

    private var clientSerialNumber = 0

    @Volatile
    private var bindProxyPort = 0

    @Volatile
    private var isStart = false

    override fun getStatus(): IProxyBind.BindStatus {
        return if (bindProxyPort == 0) {
            IProxyBind.BindStatus.UNBOUND
        } else {
            IProxyBind.BindStatus.BOUND
        }
    }

    @Synchronized
    override fun bind(port: Int) {
        if (!isStart) {
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

    fun release() {
        if (currentTcpSendThread != null) {
            isStart = false
            if (currentTcpSendThread?.state == Thread.State.TIMED_WAITING) {
                currentTcpSendThread?.interrupt()
            }
        }
    }

    fun sendPacket(packet: TCPPacket) {
        packetList.add(packet)
        if (currentTcpSendThread?.state == Thread.State.TIMED_WAITING) {
            currentTcpSendThread?.interrupt()
        }
    }

    private fun sendRun() {
        currentTcpSendThread = Thread.currentThread()
        while (isStart) {
            if (packetList.isNotEmpty()) {
                val packet = packetList.removeFirst()
                val targetAddress = packet.getTargetAddress()
                val targetPort = packet.getTargetPort()
                if (packet.isControlFlag(TCPPacket.ControlFlag.SYN)){
                    if (connect(targetAddress, targetPort)) {
                        sendSynAckPacket(packet, targetAddress, targetPort)
                    }
                }else if (packet.isControlFlag(TCPPacket.ControlFlag.RST)){
                    Log.e(tag, "客户端重定向")
                    sendRstAckPacket(packet, targetAddress, targetPort)
                    Thread.sleep(200)
                    throw RuntimeException("调试失败")
                }else if (packet.isControlFlag(TCPPacket.ControlFlag.FIN)){
                    throw RuntimeException("调试失败")
                }
                else if (packet.isControlFlag(TCPPacket.ControlFlag.ACK) && packet.getVerifySerialNumber() == serverSerialNumber + 1){
                    Log.e(tag, "客户端确认接收到${serverSerialNumber}")
                    throw RuntimeException("调试成功")
                }
            } else {
                try {
                    Thread.sleep(liveTime)
                    break
                } catch (e: InterruptedException) {
                }
                Log.e(tag, "线程被唤醒")
            }
        }
    }

    private fun receiveRun() {
        currentTcpReceiveThread = Thread.currentThread()
        while (isStart) {

        }
    }

    private fun connect(address: String, port: Int): Boolean{
        if (!tcpSocket.isConnected){
            //未连接，进行三次握手
            val remoteAddr = InetSocketAddress(address, port)
            try {
                tcpSocket.connect(remoteAddr, 2000)
            }catch (e: Exception){
                e.printStackTrace()
                //连接失败
            }
            return tcpSocket.isConnected
        }else{
            return false
        }
    }

    private fun sendRstAckPacket(tcpPacket: TCPPacket, address: String, port: Int){
        serverSerialNumber++
        clientSerialNumber = tcpPacket.getSerialNumber()
        val packet = TCPPacket()
        packet.setTargetPort(bindProxyPort)
        packet.setTargetAddress(LocalVpnConfig.PROXY_ADDRESS)
        packet.setSourcePort(port)
        packet.setSourceAddress(address)
        packet.setOffsetFrag(0)
        packet.setFlag(2)
        packet.setTimeToLive(64)
        packet.setIdentification(Random(System.currentTimeMillis()).nextInt().toShort())
        packet.setVerifySerialNumber(clientSerialNumber + 1)
        packet.setSerialNumber(serverSerialNumber)
        packet.setControlFlag(TCPPacket.ControlFlag.ACK)
        packet.setRawData(packet.getRawData())
        handleTun.outputData(packet)
    }

    private fun sendSynAckPacket(tcpPacket: TCPPacket, address: String, port: Int) {
        serverSerialNumber = Random.nextInt()
        clientSerialNumber = tcpPacket.getSerialNumber()
        val packet = TCPPacket()
        packet.setTargetPort(bindProxyPort)
        packet.setTargetAddress(LocalVpnConfig.PROXY_ADDRESS)
        packet.setSourcePort(port)
        packet.setSourceAddress(address)
        packet.setOffsetFrag(0)
        packet.setFlag(2)
        packet.setTimeToLive(64)
        packet.setIdentification(Random(System.currentTimeMillis()).nextInt().toShort())
        packet.setVerifySerialNumber(clientSerialNumber + 1)
        packet.setSerialNumber(serverSerialNumber)
        packet.setControlFlag(TCPPacket.ControlFlag.ACK)
        packet.setControlFlag(TCPPacket.ControlFlag.SYN)

        val packetMss = tcpPacket.getMSS()
        if (packetMss != 0){
            packet.setMSS(packetMss)
        }

        val packetWsopt = tcpPacket.getWSOPT()
        if (packetWsopt != 0){
            packet.setWSOPT(packetWsopt.toByte())
        }

        if (tcpPacket.getSACK_P()){
            packet.setSACK_P(true)
        }

//        val packetTsopt = tcpPacket.getTSOPT()
//        if (packetTsopt[0] != 0L){
//            packet.setTSOPT(packetTsopt[0])
//        }

        tcpPacket.setRawData(tcpPacket.getRawData())
        Log.e("Test","sny \n ${tcpPacket.optionsToString()}")

        Log.e("Test","sny ack \n ${packet.optionsToString()}")

        handleTun.outputData(packet)
    }
}