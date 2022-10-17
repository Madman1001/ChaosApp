package com.lhr.vpn.socks.socket.action

import com.lhr.vpn.socks.net.v4.NetTcpPacket
import com.lhr.vpn.socks.net.v4.SIGN_ACK
import com.lhr.vpn.socks.net.v4.SIGN_PSH
import com.lhr.vpn.socks.net.v4.SIGN_SYN
import com.lhr.vpn.socks.net.v4.proto.STATE_ESTABLISHED
import com.lhr.vpn.socks.net.v4.proto.STATE_LISTEN
import com.lhr.vpn.socks.net.v4.proto.STATE_SYN_REVD
import com.lhr.vpn.socks.socket.TcpTunSocket
import com.lhr.vpn.util.PacketV4Factory
import kotlin.random.Random

/**
 * @CreateDate: 2022/10/17
 * @Author: mac
 * @Description: 握手动作
 */
class ShakeHandsAction(private val tcpTunSocket: TcpTunSocket): ITcpAction {
    private val targetPort: Int get() = tcpTunSocket.bean.targetPort

    private val sourcePort: Int get() = tcpTunSocket.bean.sourcePort

    private var state: Int
        get() = tcpTunSocket.currentState
        set(value) { tcpTunSocket.currentState = value }

    private var sqeNumber: Int
        get() = tcpTunSocket.serverSerialNumber.toInt()
        set(value) {
            tcpTunSocket.serverSerialNumber = value.toUInt().toLong()
        }

    private var ackNumber: Int
        get() = tcpTunSocket.clientSerialNumber.toInt()
        set(value) {
            tcpTunSocket.clientSerialNumber = value.toUInt().toLong()
        }

    override fun receive(packet: NetTcpPacket) {
        tcpTunSocket.receivePacket(packet)
    }

    override fun send(packet: NetTcpPacket) {
        val controlSign = packet.controlSign
        when(state){
            STATE_LISTEN -> {
                if (controlSign and SIGN_SYN != 0){
                    //发送
                    sqeNumber = Random.nextInt()
                    ackNumber = packet.sequenceNumber

                    val tcpPacket = PacketV4Factory.createTcpPacket(
                        data = ByteArray(0),
                        sourcePort = targetPort,
                        targetPort = sourcePort
                    )
                    tcpPacket.windowSize = packet.windowSize
                    tcpPacket.controlSign = (tcpPacket.controlSign or SIGN_SYN or SIGN_ACK)
                    tcpPacket.sequenceNumber = sqeNumber
                    tcpPacket.ackSequenceNumber = ackNumber + 1

                    tcpTunSocket.currentState = STATE_SYN_REVD
                }
            }

            STATE_SYN_REVD -> {
                if (ackNumber == packet.sequenceNumber + 1 && hasSign(controlSign, SIGN_ACK)){
                    state = STATE_ESTABLISHED
                }
            }

            STATE_ESTABLISHED -> {
                if (ackNumber == packet.sequenceNumber + 1){
                    if (hasSign(controlSign, SIGN_PSH))
                }
            }
        }
    }

    private fun hasSign(controlSign: Int, sign: Int): Boolean{
        return controlSign and sign != 0
    }
}