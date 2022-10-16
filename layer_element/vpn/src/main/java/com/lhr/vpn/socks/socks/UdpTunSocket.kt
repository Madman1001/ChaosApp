package com.lhr.vpn.socks.socks

import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.NetProxyBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class UdpTunSocket(
    val bean: NetProxyBean,
    private val tunSocks: UdpSocks,
    internal val socket: DatagramSocket
) {
    private val tag = this::class.java.simpleName

    private val receivePacket = DatagramPacket(ByteArray(1024), 1024)

    private var receiveJob: Job? = null

    fun sendPacket(packet: DatagramPacket){
        RunPool.execute(TunRunnable("$tag$this-out"){
            socket.send(packet)
        })

        if (receiveJob == null || receiveJob?.isActive != true){
            startReceive()
        }
    }

    /**
     * 启动接收线程
     */
    private fun startReceive(){
        receiveJob?.cancel()

        val inputRunnable = TunRunnable("$tag$this-in"){
            while (true){
                socket.receive(receivePacket)
                val data = ByteArray(receivePacket.length)
                System.arraycopy(receivePacket.data, 0, data, 0, data.size)
                tunSocks.socksToTun(bean, data)
            }
        }
        receiveJob = GlobalScope.launch(Dispatchers.IO){
            inputRunnable.run()
        }
    }
}