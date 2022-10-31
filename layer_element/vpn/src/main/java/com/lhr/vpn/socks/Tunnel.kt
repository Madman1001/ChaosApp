package com.lhr.vpn.socks

import java.nio.channels.SocketChannel

/**
 * @author lhr
 * @date 31/10/2022
 * @des
 */
class Tunnel(val channel: SocketChannel){
    var bindChannel: Tunnel? = null
}
fun Tunnel.bind(tunnel: Tunnel){
    this.bindChannel = tunnel
    tunnel.bindChannel = this
}
