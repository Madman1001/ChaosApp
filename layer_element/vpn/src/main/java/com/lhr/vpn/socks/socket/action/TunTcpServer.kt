package com.lhr.vpn.socks.socket.action

import android.util.Log
import com.lhr.vpn.socks.net.v4.*
import com.lhr.vpn.socks.net.v4.proto.*
import com.lhr.vpn.socks.socket.TcpTunSocket
import com.lhr.vpn.util.PacketV4Factory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import kotlin.random.Random

/**
 * @CreateDate: 2022/10/17
 * @Author: mac
 * @Description: 握手动作
 */
class TunTcpServer(private val tcpTunSocket: TcpTunSocket): ITcpAction {
    private val tag = this::class.java.simpleName

    private val sourceAddress: InetAddress get() = tcpTunSocket.bean.sourceAddress

    private val targetAddress: InetAddress get() = tcpTunSocket.bean.targetAddress

    private val targetPort: Int get() = tcpTunSocket.bean.targetPort

    private val sourcePort: Int get() = tcpTunSocket.bean.sourcePort

    private val socket = tcpTunSocket.socket

    private var sqeNumber = 0L

    private var ackNumber = 0L

    @Volatile
    private var state = STATE_LISTEN

    override fun receive(packet: NetTcpPacket) {
        tcpTunSocket.receivePacket(packet)
    }

    override fun send(packet: NetTcpPacket) {
        val controlSign = packet.controlSign
        Log.e(tag, "${Thread.currentThread().name} packet $controlSign  $state")
        when(state){
            STATE_LISTEN -> {
                if (hasSign(controlSign, SIGN_SYN)){
                    //发送
                    sqeNumber = Random.nextInt().toUInt().toLong()
                    ackNumber = packet.sequenceNumber.toUInt().toLong()

                    val tcpPacket = PacketV4Factory.createTcpPacket(
                        data = ByteArray(0),
                        sourcePort = targetPort,
                        targetPort = sourcePort
                    )
                    tcpPacket.windowSize = packet.windowSize
                    tcpPacket.controlSign = (tcpPacket.controlSign or SIGN_SYN or SIGN_ACK)
                    tcpPacket.sequenceNumber = sqeNumber.toInt()
                    tcpPacket.ackSequenceNumber = (ackNumber + 1).toInt()

                    state = STATE_SYN_REVD
                    tcpTunSocket.receivePacket(tcpPacket)
                }
            }

            STATE_SYN_REVD -> {
                if (!isValid(packet)) return

                if (hasSign(controlSign, SIGN_ACK)){
                    state = STATE_ESTABLISHED
                }
            }

            STATE_ESTABLISHED -> {
                if (!isValid(packet)) return

                if (hasSign(controlSign, SIGN_PSH)){
                    ackNumber += packet.data.size
                    sqeNumber = packet.sequenceNumber.toUInt().toLong()
                    if (!socket.isConnected){
                        socket.connect(InetSocketAddress(targetAddress, targetPort), 3000)
                        tcpTunSocket.startReceive()
                    }
                    socket.getOutputStream().write(packet.data)
                }

                val tcpPacket = PacketV4Factory.createTcpPacket(
                    data = ByteArray(0),
                    sourcePort = targetPort,
                    targetPort = sourcePort
                )
                tcpPacket.windowSize = packet.windowSize
                tcpPacket.controlSign = (tcpPacket.controlSign or  SIGN_ACK)
                tcpPacket.sequenceNumber = sqeNumber.toInt()
                tcpPacket.ackSequenceNumber = (ackNumber + 1).toInt()

                if (hasSign(controlSign, SIGN_FIN)){
                    state = STATE_CLOSE_WAIT
                    tcpPacket.controlSign = (tcpPacket.controlSign or  SIGN_FIN)
                    tcpTunSocket.receivePacket(tcpPacket)
                    state = STATE_LAST_ACK
                } else {
                    tcpTunSocket.receivePacket(tcpPacket)
                }
            }

            STATE_LAST_ACK -> {
                if (!isValid(packet)) return

                if (hasSign(controlSign, SIGN_ACK)){
                    state = STATE_CLOSED
                }
            }
        }
    }

    private fun hasSign(controlSign: Int, sign: Int): Boolean{
        return controlSign and sign != 0
    }

    private fun isValid(tcpPacket: NetTcpPacket): Boolean{
        return ackNumber + 1 == tcpPacket.sequenceNumber.toUInt().toLong()
    }
}