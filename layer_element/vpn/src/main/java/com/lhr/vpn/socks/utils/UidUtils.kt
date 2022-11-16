package com.lhr.vpn.socks.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.system.OsConstants.*
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress


/**
 * @CreateDate: 2022/11/14
 * @Author: mac
 * @Description: Get uid according to Quintuple
 */
object UidUtils {
    private val TAG = this::class.java.simpleName
    private val fieldsRegex =
        Regex("^\\s*(\\d+): ([0-9A-F]+):(....) ([0-9A-F]+):(....) (..) (?:\\S+ ){3}\\s*(\\d+)\\s+\\d+\\s+(\\d+).*$")
    const val UID_UNKNOWN = -1

    fun getUidByNetLink(
        context: Context,
        protocol: Int,
        sAddress: InetSocketAddress,
        dAddress: InetSocketAddress
    ): Int {
        var uid = UID_UNKNOWN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uid = getNetLinkUidByConnectManager(context, protocol, sAddress, dAddress)
        }
        val saddrHex = makeProcIpHexString(sAddress.address)
        val daddrHex = makeProcIpHexString(dAddress.address)
        Log.e(TAG, "$saddrHex:$daddrHex")
        if (uid == UID_UNKNOWN && saddrHex.length == "FFFFFFFF".length) {
            uid = getNetLinkUidByProc(
                IPPROTO_IP,
                protocol,
                saddrHex,
                sAddress.port,
                daddrHex,
                dAddress.port
            )
        }
        if (uid == UID_UNKNOWN) {
            uid = getNetLinkUidByProc(
                IPPROTO_IPV6,
                protocol,
                "0000000000000000FFFF0000$saddrHex",
                sAddress.port,
                "0000000000000000FFFF0000$daddrHex",
                dAddress.port
            )
        }

        Log.d(TAG, "netlink(${sAddress.address}:${sAddress.port}=${dAddress.address}:${dAddress.port}) uid is $uid")
        return uid
    }

    fun getNetLinkUidByProc(
        ipver: Int,
        protocol: Int,
        saddr: String,
        sport: Int,
        daddr: String,
        dport: Int
    ): Int {
        val procFile = when (protocol) {
            IPPROTO_TCP -> if (IPPROTO_IP == ipver) "/proc/net/tcp" else "/proc/net/tcp6"
            IPPROTO_UDP -> if (IPPROTO_IP == ipver) "/proc/net/udp" else "/proc/net/udp6"
            IPPROTO_ICMP, IPPROTO_ICMPV6 -> if (IPPROTO_IP == ipver) "/proc/net/icmp" else "/proc/net/icmp6"
            else -> return UID_UNKNOWN
        }
        val lineBuffer = BufferedReader(InputStreamReader(FileInputStream(procFile)))

        val zero = if (ipver == IPPROTO_IP) "00000000" else "00000000000000000000000000000000"

        var uid = UID_UNKNOWN
        var line: String? = lineBuffer.readLine()
        Log.e(TAG, line + "")
        //skip first
        if (line != null) line = lineBuffer.readLine()

        while (line != null) {
            val result = fieldsRegex.matchEntire(line)?.groupValues
            if (result != null) {
                debugLog(result)
                if (sport == Integer.parseInt(result[3], 16)
                    && (saddr == result[2] || result[2] == zero)
                    && (dport == Integer.parseInt(result[5], 16) || Integer.parseInt(result[5], 16) == 0)
                    && (daddr == result[4] || result[4] == zero)
                ) {
                    uid = Integer.parseInt(result[7])
                    break
                }
            }
            line = lineBuffer.readLine()
        }
        lineBuffer.close()
        return uid
    }

    // from NetGuard
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getNetLinkUidByConnectManager(
        context: Context,
        protocol: Int,
        sAddress: InetSocketAddress,
        dAddress: InetSocketAddress
    ): Int {
        if (protocol != 6 /* TCP */ && protocol != 17 /* UDP */) return UID_UNKNOWN

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return UID_UNKNOWN
        return cm.getConnectionOwnerUid(protocol, sAddress, dAddress)
    }

    private fun makeProcIpHexString(address: InetAddress): String {
        val data = address.address
        val sb = StringBuilder()
        for (i in (data.size - 1) downTo 0) {
            sb.append(String.format("%02X", data[i]))
        }
        return sb.toString()
    }

    private fun debugLog(result: List<String>){
        Log.e(
            TAG, """
                NetInfo(
                    slot = ${result[1]},
                    localAddress = ${result[2]},
                    localPort = ${result[3]},
                    remoteAddress = ${result[4]},
                    remotePort = ${result[5]},
                    state = ${result[6]},
                    uid = ${result[7]},
                    inode = ${result[8]}
                )
            """.trimIndent()
        )
    }
}