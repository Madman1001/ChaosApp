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
import java.net.InetSocketAddress


/**
 * @CreateDate: 2022/11/14
 * @Author: mac
 * @Description: obtain uid
 */
object UidUtils {
    private val TAG = this::class.java.simpleName

    const val UID_UNKNOWN = -1

    fun getUidByNetLink(context: Context,
                        ipver: Int,
                        protocol: Int,
                        saddr: String,
                        sport: Int,
                        daddr: String,
                        dport: Int): Int{
        var uid = UID_UNKNOWN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uid = getNetLinkUidByConnectManager(context, protocol, saddr, sport, daddr, dport)
        }
        if (uid == UID_UNKNOWN){
            uid = getNetLinkUidByProc(ipver, protocol, saddr, sport, daddr, dport)
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

        var line: String? = lineBuffer.readLine()

        while (line != null){
            Log.e(TAG, line)
            line = lineBuffer.readLine()
        }
        lineBuffer.close()
        return UID_UNKNOWN
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