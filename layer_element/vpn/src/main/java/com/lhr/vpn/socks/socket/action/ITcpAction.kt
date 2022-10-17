package com.lhr.vpn.socks.socket.action

import com.lhr.vpn.socks.net.v4.NetTcpPacket

/**
 * @CreateDate: 2022/10/17
 * @Author: mac
 * @Description: Tcp状态机动作模拟接口
 */
interface ITcpAction {
    /**
     * 接收数据包
     */
    fun receive(packet: NetTcpPacket)

    /**
     * 发送数据包
     */
    fun send(packet: NetTcpPacket)
}