package com.lhr.vpn.socks

import android.util.Log
import com.lhr.vpn.socks.socket.ProxyRouteSession
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * @author lhr
 * @date 23/10/2022
 * @des 全局代理表
 */
object SessionTable {
    private val tag = this::class.java.simpleName

    //tcp 注册表
    private val tcpTunSocketMap by lazy { ConcurrentHashMap<String, ProxyRouteSession>() }

    //udp 注册表
    private val udpTunSocketMap by lazy { ConcurrentHashMap<String, ProxyRouteSession>() }

    fun obtainTcpSession(key: String): ProxyRouteSession? {
        return tcpTunSocketMap[key]
    }

    fun obtainUdpSession(key: String): ProxyRouteSession? {
        return udpTunSocketMap[key]
    }

    fun registerUdpSession(key: String, session: ProxyRouteSession) {
        val ss = udpTunSocketMap[key]
        if (ss != session) {
            ss?.proxyTunSocket?.close()
        }
        udpTunSocketMap[key] = session
        Log.w(tag, "register udp proxy $key -> ${session.proxyTunSocket}")
    }

    fun registerTcpSession(key: String, session: ProxyRouteSession) {
        val ss = tcpTunSocketMap[key]
        if (ss != session) {
            ss?.proxyTunSocket?.close()
        }
        tcpTunSocketMap[key] = session
        Log.w(tag, "register tcp proxy $key -> ${session.proxyTunSocket}")
    }

    fun unregisterTcpSession(session: ProxyRouteSession){
        unregisterTcpSession(createSessionKey(session))
        session.proxyTunSocket?.let {
            unregisterTcpSession(it.createLocalKey())
        }
    }

    fun unregisterTcpSession(key: String) {
        val ss = tcpTunSocketMap.remove(key)
        ss?.state = ProxyRouteSession.STATE_INVALID
        ss?.proxyTunSocket?.close()
        Log.w(tag, "unregister tcp proxy $key -> ${ss?.proxyTunSocket}")
    }

    fun unregisterUdpSession(session: ProxyRouteSession){
        unregisterUdpSession(createSessionKey(session))
        session.proxyTunSocket?.let {
            unregisterUdpSession(it.createLocalKey())
        }
    }

    fun unregisterUdpSession(key: String) {
        val ss = udpTunSocketMap.remove(key)
        ss?.state = ProxyRouteSession.STATE_INVALID
        ss?.proxyTunSocket?.close()
        Log.w(tag, "unregister udp proxy $key -> ${ss?.proxyTunSocket}")
    }

    fun createSessionKey(session: ProxyRouteSession): String {
        return createSessionKey(
            session.sourceAddress,
            session.sourcePort,
            session.targetAddress,
            session.targetPort
        )
    }

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