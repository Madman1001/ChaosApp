package com.lhr.vpn.proxy

import com.lhr.vpn.protocol.IPPacket

/**
 * @author lhr
 * @date 2021/12/30
 * @des 客户端代理接口
 */
interface IProxyClient {

    /**
     * 数据报内部发往外部
     */
    fun internalToExternal(packet: IPPacket)

    /**
     * 数据报外部发往内部
     */
    fun externalToInternal(packet: IPPacket)
}