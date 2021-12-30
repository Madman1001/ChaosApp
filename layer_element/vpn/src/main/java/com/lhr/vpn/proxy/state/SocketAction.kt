package com.lhr.vpn.proxy.state

import com.lhr.vpn.protocol.IPPacket

/**
 * @author lhr
 * @date 2021/12/29
 * @des Socket状态机模式动作接口
 */
interface SocketAction {
    /**
     * 接收数据包
     */
    fun receive(packet: IPPacket)

    /**
     * 发送数据包
     */
    fun send(packet: IPPacket)
}