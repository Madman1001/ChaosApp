package com.lhr.vpn.socks

import android.util.Log
import com.lhr.vpn.socks.socket.ProxySession
import java.util.concurrent.ConcurrentHashMap

/**
 * @author lhr
 * @date 23/10/2022
 * @des 全局代理表
 */
object SessionTable {
    private val tag = this::class.java.simpleName

    //tcp 注册表
    private val tcpTunSocketMap by lazy { ConcurrentHashMap<String, ProxySession>() }

    //udp 注册表
    private val udpTunSocketMap by lazy { ConcurrentHashMap<String, ProxySession>() }

    fun obtainTcpSession(key: String): ProxySession? {
        return tcpTunSocketMap[key]
    }

    fun registerTcpSession(key: String, session: ProxySession) {
        val ss = tcpTunSocketMap[key]
        tcpTunSocketMap[key] = session
        if (ss != session && ss != null) {
            Log.e(tag, "replace tcp proxy session $ss ${ss.mainKey}|${ss.localKey}")
        }
    }

    fun unregisterTcpSession(key: String): ProxySession? {
        return tcpTunSocketMap.remove(key)
    }

    fun obtainUdpSession(key: String): ProxySession? {
        return udpTunSocketMap[key]
    }

    fun registerUdpSession(key: String, session: ProxySession) {
        val ss = udpTunSocketMap[key]
        udpTunSocketMap[key] = session
        if (ss != session && ss != null) {
            Log.e(tag, "replace udp proxy session $ss ${ss.mainKey}|${ss.localKey}")
        }
    }

    fun unregisterUdpSession(key: String): ProxySession? {
        return udpTunSocketMap.remove(key)
    }

    fun clearAllSession(){
        for (entry in udpTunSocketMap) {
            entry.value.proxyTunSocket?.close()
        }
        udpTunSocketMap.clear()

        for (entry in tcpTunSocketMap) {
            entry.value.proxyTunSocket?.close()
        }
        tcpTunSocketMap.clear()
    }
}