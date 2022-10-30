package com.lhr.vpn.socks.socket

import com.lhr.vpn.socks.net.v4.NetIpPacket
import java.io.Closeable

/**
 * @author lhr
 * @date 15/10/2022
 * @des 中转处理接口
 */
interface ITunSocket: Closeable {
    fun handlePacket(packet: NetIpPacket)
}