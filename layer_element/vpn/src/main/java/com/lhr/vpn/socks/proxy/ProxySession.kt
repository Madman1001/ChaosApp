package com.lhr.vpn.socks.proxy

import java.net.InetSocketAddress
import java.util.*

/**
 * @author lhr
 * @date 22/10/2022
 * @des 代理session
 * @param uid the uid of the owner of a session
 * @param saddr
 * @param daddr
 */
class ProxySession(
    val uid: Int,
    val saddr: InetSocketAddress,
    val daddr: InetSocketAddress,
) {
    /**
     * A unique, universal identifier for the session data structure.
     */
    val sessionid: Long = Random().nextLong()

    /**
     * total number of bytes sent
     */
    var sentBytes: Long = 0

    /**
     * total number of bytes receive
     */
    var rcvdBytes: Long = 0

    /**
     * TCP or UDP
     */
    var type = Type.TCP

    /**
     * Defines whether the session state.
     */
    var state = State.READY

    val saddrString = saddr.address.hostAddress

    val sport = saddr.port.toShort()

    val daddrString = daddr.address.hostAddress

    val dport = daddr.port.toShort()
}

enum class Type{
    TCP, UDP
}

enum class State{
    READY, STARTING, RUNNING, STOPPING
}