package com.lhr.vpn.proxy.state

import android.util.Log
import com.lhr.vpn.protocol.IPPacket

/**
 * @author lhr
 * @date 2021/12/30
 * @des udp等待响应动作
 */
class UselessAction(private val tag: String): SocketAction {
    override fun receive(packet: IPPacket) {
        Log.e(tag, "执行无用操作receive")
    }

    override fun send(packet: IPPacket) {
        Log.e(tag, "执行无用操作send")
    }
}