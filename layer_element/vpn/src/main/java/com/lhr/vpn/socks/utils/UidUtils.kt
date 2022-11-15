package com.lhr.vpn.socks.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.system.OsConstants.*
import android.util.Log
import androidx.annotation.RequiresApi
import com.lhr.vpn.toIpHexString
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress


/**
 * @CreateDate: 2022/11/14
 * @Author: mac
 * @Description: obtain uid
 */
object UidUtils {
    private val TAG = this::class.java.simpleName
    private val fieldsRegex =
        Regex("^\\s*(\\d+): ([0-9A-F]+):(....) ([0-9A-F]+):(....) (..) (?:\\S+ ){3}\\s*(\\d+)\\s+\\d+\\s+(\\d+).*$")
    const val UID_UNKNOWN = -1

    fun getUidByNetLink(context: Context,
                        ipver: Int,
                        protocol: Int,
                        saddr: String,
                        sport: Int,
                        daddr: String,
                        dport: Int): Int{
        var uid = UID_UNKNOWN
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            uid = getNetLinkUidByConnectManager(context, protocol, saddr, sport, daddr, dport)
//        }
        val saddrHex = InetAddress.getByName(saddr).address.toIpHexString()
        val daddrHex = InetAddress.getByName(daddr).address.toIpHexString()
        Log.e(TAG, saddrHex + ":" + daddrHex)
        if (uid == UID_UNKNOWN){
            uid = getNetLinkUidByProc(ipver, protocol, saddrHex, sport, daddrHex, dport)
            if (uid == UID_UNKNOWN && ipver == IPPROTO_IP){
                uid = getNetLinkUidByProc(IPPROTO_IPV6,
                    protocol,
                    "0000000000000000FFFF0000$saddrHex",
                    sport,
                    "0000000000000000FFFF0000$daddrHex",
                    dport
                )
            }
        }
        return uid
    }

    fun getNetLinkUidByProc(ipver: Int, protocol: Int, saddr: String, sport: Int, daddr: String, dport: Int): Int{
        val procFile = when(protocol){
            IPPROTO_TCP -> if (IPPROTO_IP == ipver) "/proc/net/tcp" else "/proc/net/tcp6"
            IPPROTO_UDP -> if (IPPROTO_IP == ipver) "/proc/net/udp" else "/proc/net/udp6"
            IPPROTO_ICMP, IPPROTO_ICMPV6 -> if (IPPROTO_IP == ipver) "/proc/net/icmp" else "/proc/net/icmp6"
            else -> return UID_UNKNOWN
        }
        val lineBuffer = BufferedReader(InputStreamReader(FileInputStream(procFile)))

        var uid = UID_UNKNOWN
        var line: String? = lineBuffer.readLine()
        Log.e(TAG, line + "")
        //skip first
        if (line != null) line = lineBuffer.readLine()

        while (line != null){
            val result = fieldsRegex.matchEntire(line)?.groupValues
            if (result != null){
                Log.e(
                    TAG,"""
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
            """.trimIndent())

                if (saddr == result[2]
                    && sport == java.lang.Integer.parseInt(result[3], 16)
                    && daddr == result[4]
                    && dport == java.lang.Integer.parseInt(result[5], 16)){
                    uid = java.lang.Integer.parseInt(result[7])
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
    fun getNetLinkUidByConnectManager(context: Context, protocol: Int, saddr: String, sport: Int, daddr: String, dport: Int): Int{
        if (protocol != 6 /* TCP */ && protocol != 17 /* UDP */) return UID_UNKNOWN

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return UID_UNKNOWN

        val local = InetSocketAddress(saddr, sport)
        val remote = InetSocketAddress(daddr, dport)

        Log.d(TAG, "Get uid local=$local remote=$remote")
        return cm.getConnectionOwnerUid(protocol, local, remote)
    }
}