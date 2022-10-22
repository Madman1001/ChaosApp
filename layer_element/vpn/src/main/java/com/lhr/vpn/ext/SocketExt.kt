package com.lhr.vpn.ext

import java.net.DatagramSocket
import java.net.Socket

/**
 * @author lhr
 * @date 23/10/2022
 * @des socket 扩展工具
 */
fun Socket.isValid(): Boolean{
    return this.isConnected && !this.isInputShutdown && !this.isOutputShutdown
}

fun DatagramSocket.isValid(): Boolean{
    return !this.isClosed
}