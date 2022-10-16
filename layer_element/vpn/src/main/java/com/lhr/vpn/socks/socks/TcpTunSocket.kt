package com.lhr.vpn.socks.socks

import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.NetProxyBean
import com.lhr.vpn.socks.net.v4.proto.TcpStateMachine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.Socket
import java.nio.ByteBuffer

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
    private val tcpStateMachine by lazy { TcpStateMachine(true) }

    fun sendPacket(data: ByteArray) {
        RunPool.execute(TunRunnable("$tag$this-out") {
            socketChannel.write(ByteBuffer.wrap(data))
        })

        if (receiveJob == null || receiveJob?.isActive != true) {
            startReceive()
        }
    }

    /**
     * 启动接收线程
     */
    private fun startReceive() {
        receiveJob?.cancel()

        val inputRunnable = TunRunnable("$tag$this-in") {
            while (true) {
                receiveBuffer.rewind()
                val len = socketChannel.read(receiveBuffer)
                val data = ByteArray(len)
                receiveBuffer.rewind()
                receiveBuffer.get(data)
                tcpSocks.socksToTun(bean, data)
            }
        }
        receiveJob = GlobalScope.launch(Dispatchers.IO) {
            inputRunnable.run()
        }
    }
}