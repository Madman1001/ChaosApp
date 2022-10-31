package com.lhr.vpn.socks.proxy

import java.net.InetAddress
import java.util.*

/**
 * @author lhr
 * @date 22/10/2022
 * @des 代理session
 */
class ProxySession(
    val address: InetAddress,
    val port: Int,
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
    var type = TYPE_TCP

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

    companion object {
        @JvmField
        val TYPE_TCP = 1

        @JvmField
        val TYPE_UDP = 2

        @JvmField
        val STATE_VALID = 1

        @JvmField
        val STATE_INVALID = -1

        @JvmStatic
        fun createSessionKey(
            source: InetAddress,
            sourcePort: Int,
            target: InetAddress,
            targetPort: Int
        ): String {
            return StringBuilder()
                .append(source.hostAddress).append(":").append(sourcePort)
                .append("-")
                .append(target.hostAddress).append(":").append(targetPort)
                .toString()
        }
    }
}