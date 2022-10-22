package com.lhr.vpn.socks.socket

import java.net.InetAddress
import java.util.*

/**
 * @author lhr
 * @date 22/10/2022
 * @des 代理session
 */
class ProxyRouteSession(
    val sourceAddress: InetAddress,
    val sourcePort: Int,
    val targetAddress: InetAddress,
    val targetPort: Int
) {
    /**
     * A unique, universal identifier for the session data structure.
     */
    val sessionid: Long = Random().nextLong()

    /**
     * Principal
     * Set to the user's distinguished name (DN) or the application's principal name.
     */
    var name = ""

    /**
     * USER or APPLICATION
     */
    var type = TYPE_USER

    /**
     * Defines whether the session is valid or invalid.
     */
    var state = STATE_VALID

    /**
     * Maximum number of minutes without activity before the session will expire and the user must reauthenticate.
     */
    var maximumIdleTime = 0L

    /**
     * Maximum number of minutes (activity or no activity) before the session expires and the user must reauthenticate.
     */
    var maximumSessionTime = 0L

    /**
     * Maximum number of minutes before the client contacts OpenSSO Enterprise to refresh cached session information.
     */
    var maximumCachingTime = 0L

    /**
     * 代理tun socket
     */
    var proxyTunSocket: ITunSocket? = null

    companion object {
        @JvmField
        val TYPE_USER = 1

        @JvmField
        val TYPE_APPLICATION = 2

        @JvmField
        val STATE_VALID = 1

        @JvmField
        val STATE_INVALID = -1
    }
}