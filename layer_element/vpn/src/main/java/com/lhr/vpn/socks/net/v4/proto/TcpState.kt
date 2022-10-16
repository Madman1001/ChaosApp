package com.lhr.vpn.socks.net.v4.proto

/**
 * @author lhr
 * @date 16/10/2022
 * @des tcp状态
 */
data class TcpState(val state: Int, val routes: List<TcpStateRoute>)