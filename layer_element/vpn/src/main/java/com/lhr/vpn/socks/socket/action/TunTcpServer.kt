package com.lhr.vpn.socks.socket.action

import android.util.Log
import com.lhr.vpn.socks.net.v4.*
import com.lhr.vpn.socks.net.v4.proto.*
import com.lhr.vpn.socks.socket.TcpTunSocket
import com.lhr.vpn.util.PacketV4Factory
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.random.Random

/**
 * @CreateDate: 2022/10/17
 * @Author: mac
 * @Description: 服务端中转
 */
class TunTcpServer(private val tcpTunSocket: TcpTunSocket): ITcpAction {
    private val tag = this::class.java.simpleName

    private val sourceAddress: InetAddress get() = tcpTunSocket.bean.sourceAddress

    private val targetAddress: InetAddress get() = tcpTunSocket.bean.targetAddress

    private val targetPort: Int get() = tcpTunSocket.bean.targetPort

    private val sourcePort: Int get() = tcpTunSocket.bean.sourcePort

    private val socket = tcpTunSocket.socket

    private var baseSqeNumber = 0L

    private var baseAckNumber = 0L

    private var seqNumber = 0L

    private var ackNumber = 0L

    @Volatile
    private var state = STATE_LISTEN

    override fun receive(packet: NetTcpPacket) {
        packet.windowSize = packet.windowSize
        packet.controlSign = (packet.controlSign or SIGN_PSH or SIGN_ACK)
        packet.sequenceNumber = seqNumber.toInt()
        packet.ackSequenceNumber = (ackNumber + 1).toInt()
        packet.windowSize
        tcpTunSocket.receivePacket(packet)

        seqNumber += packet.data.size
    }

    override fun send(packet: NetTcpPacket) {
        val controlSign = packet.controlSign
        when(state){
            STATE_LISTEN -> {
                if (hasSign(controlSign, SIGN_SYN)){
                    //发送
                    baseSqeNumber = Random.nextInt().toUInt().toLong()
                    baseAckNumber = packet.sequenceNumber.toUInt().toLong()

                    seqNumber = baseSqeNumber
                    ackNumber = baseAckNumber

                    val tcpPacket = PacketV4Factory.createTcpPacket(
                        data = ByteArray(0),
                        sourcePort = targetPort,
                        targetPort = sourcePort
                    )
                    tcpPacket.windowSize = packet.windowSize
                    tcpPacket.controlSign = (tcpPacket.controlSign or SIGN_SYN or SIGN_ACK)
                    tcpPacket.sequenceNumber = seqNumber.toInt()
                    tcpPacket.ackSequenceNumber = (ackNumber + 1).toInt()

                    state = STATE_SYN_REVD
                    tcpTunSocket.receivePacket(tcpPacket)
                }
            }

            STATE_SYN_REVD -> {
                if (hasSign(controlSign, SIGN_ACK)
                    && packet.ackSequenceNumber.toUInt().toLong() == seqNumber + 1) {
                    state = STATE_ESTABLISHED
                }
            }

            STATE_ESTABLISHED -> {
                if (packet.ackSequenceNumber.toUInt().toLong() != seqNumber + 1) return

                if (hasSign(controlSign, SIGN_PSH)){
                    if (!socket.isConnected){
                        socket.connect(InetSocketAddress(targetAddress, targetPort), 3000)
                        tcpTunSocket.startReceive()
                    }
                    socket.getOutputStream().write(packet.data)

                    ackNumber = packet.sequenceNumber.toUInt().toLong() + packet.data.size

                    val ackPacket = PacketV4Factory.createTcpPacket(
                        data = ByteArray(0),
                        sourcePort = targetPort,
                        targetPort = sourcePort
                    )
                    ackPacket.windowSize = packet.windowSize
                    ackPacket.controlSign = (ackPacket.controlSign or  SIGN_ACK)
                    ackPacket.sequenceNumber = seqNumber.toInt()
                    ackPacket.ackSequenceNumber = (ackNumber + 1).toInt()
                    tcpTunSocket.receivePacket(ackPacket)
                }

                if (hasSign(controlSign, SIGN_FIN)){
                    val finAckPacket = PacketV4Factory.createTcpPacket(
                        data = ByteArray(0),
                        sourcePort = targetPort,
                        targetPort = sourcePort
                    )
                    ackNumber = packet.sequenceNumber.toUInt().toLong() + packet.data.size
                    finAckPacket.windowSize = packet.windowSize
                    finAckPacket.controlSign = SIGN_ACK
                    finAckPacket.sequenceNumber = seqNumber.toInt()
                    finAckPacket.ackSequenceNumber = (ackNumber + 1).toInt()
                    tcpTunSocket.receivePacket(finAckPacket)

                    state = STATE_CLOSE_WAIT
                    val finPacket = PacketV4Factory.createTcpPacket(
                        data = ByteArray(0),
                        sourcePort = targetPort,
                        targetPort = sourcePort
                    )
                    finPacket.windowSize = packet.windowSize
                    finPacket.controlSign = (finPacket.controlSign or  SIGN_FIN)
                    finPacket.sequenceNumber = seqNumber.toInt()
                    finPacket.ackSequenceNumber = (ackNumber + 1).toInt()
                    tcpTunSocket.receivePacket(finPacket)
                    state = STATE_LAST_ACK
                }
            }

            STATE_LAST_ACK -> {
                if (hasSign(controlSign, SIGN_ACK)
                    && packet.ackSequenceNumber.toUInt().toLong() == seqNumber + 1) {
                    seqNumber = packet.ackSequenceNumber.toUInt().toLong()
                    state = STATE_CLOSED
                    socket.close()
                }
            }

            STATE_CLOSED -> {
                Log.e(tag, "连接已关闭")
            }
        }
    }

    private fun hasSign(controlSign: Int, sign: Int): Boolean{
        return controlSign and sign != 0
    }
}