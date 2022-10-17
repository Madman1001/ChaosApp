package com.lhr.vpn.socks.socket

import android.util.Log
import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.NetProxyBean
import com.lhr.vpn.socks.TcpSocks
import com.lhr.vpn.socks.net.v4.NetTcpPacket
import com.lhr.vpn.socks.net.v4.*
import com.lhr.vpn.socks.net.v4.proto.*
import com.lhr.vpn.socks.socket.action.ITcpAction
import com.lhr.vpn.socks.socket.action.ShakeHandsAction
import com.lhr.vpn.util.PacketV4Factory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.random.Random

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class TcpTunSocket(
    val bean: NetProxyBean,
    private val tcpSocks: TcpSocks,
    internal val socket: Socket
) {
    private val tag = this::class.java.simpleName

    private val socketChannel = socket.channel

    private var receiveJob: Job? = null

    private val receiveBuffer = ByteBuffer.allocate(1024)

    //内部连接的tcp状态机
    internal val tcpStateMachine by lazy { TcpStateMachine.createTcpStateMap() }

    internal var serverSerialNumber = 0L

    internal var clientSerialNumber = 0L

    internal var currentState = STATE_LISTEN

    internal var tcpAction: ITcpAction = ShakeHandsAction(this)

    fun sendPacket(packet: NetTcpPacket) {
        Log.d(tag, "sendPacket $packet")
        RunPool.execute(TunRunnable("$tag$this-out"){
            tcpAction.receive(packet)
        })
        //数据传输
        if (tcpStateMachine.getCurrentState() == STATE_ESTABLISHED){
            RunPool.execute(TunRunnable("$tag$this-out") {
                if (!socketChannel.isConnected){
                    socketChannel.connect(InetSocketAddress(bean.targetAddress, bean.targetPort))
                }
                socketChannel.write(ByteBuffer.wrap(packet.data))
            })
            if (receiveJob == null || receiveJob?.isActive != true) {
                startReceive()
            }
        } else {
            RunPool.execute(TunRunnable("$tag$this-out"){
                val sign = tcpStateMachine.recvSign(packet.controlSign)
                if (packet.controlSign and SIGN_SYN != 0){
                    serverSerialNumber = Random.nextInt().toLong()
                    clientSerialNumber = packet.sequenceNumber.toUInt().toLong()
                }
                if ((packet.controlSign and SIGN_ACK))
                if (sign == 0) return@TunRunnable

                when(tcpStateMachine.getCurrentState()){
                    STATE_LISTEN -> {
                        val tcpPacket = PacketV4Factory.createTcpPacket(
                            data = ByteArray(0),
                            sourcePort = bean.targetPort,
                            targetPort = bean.sourcePort
                        )
                        tcpPacket.windowSize = packet.windowSize
                        tcpPacket.controlSign = (tcpPacket.controlSign or sign)
                        packet.sequenceNumber = serverSerialNumber.toInt()
                        packet.ackSequenceNumber = clientSerialNumber.toInt() + 1
                        receivePacket(tcpPacket)
                    }
                }

                if (sign != 0){
                    val tcpPacket = PacketV4Factory.createTcpPacket(
                        data = ByteArray(0),
                        sourcePort = bean.targetPort,
                        targetPort = bean.sourcePort
                    )
                    tcpPacket.windowSize = packet.windowSize
                    tcpPacket.controlSign = (tcpPacket.controlSign or sign)
                    packet.sequenceNumber = serverSerialNumber.toInt()
                    packet.ackSequenceNumber = clientSerialNumber.toInt() + 1
                    receivePacket(tcpPacket)
                }
            })
        }
    }

    fun receivePacket(packet: NetTcpPacket){
        Log.d(tag, "receivePacket $packet")
        tcpSocks.socksToTun(bean, packet)
    }

    /**
     * 启动接收线程
     */
    fun startReceive() {
        receiveJob?.cancel()

        val inputRunnable = TunRunnable("$tag$this-in") {
            while (true) {
                receiveBuffer.rewind()
                val len = socketChannel.read(receiveBuffer)
                val data = ByteArray(len)
                receiveBuffer.rewind()
                receiveBuffer.get(data)
                val tcpPacket = PacketV4Factory.createTcpPacket(
                    data = data,
                    sourcePort = bean.targetPort,
                    targetPort = bean.sourcePort
                )
                receivePacket(tcpPacket)
            }
        }
        receiveJob = GlobalScope.launch(Dispatchers.IO) {
            inputRunnable.run()
        }
    }
}