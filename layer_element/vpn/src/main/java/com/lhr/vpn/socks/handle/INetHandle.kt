package com.lhr.vpn.socks.handle

/**
 * @author lhr
 * @date 13/10/2022
 * @des 数据包处理接口
 */
interface INetHandle<T> {
    /**
     * 需求处理的发送包
     * @param packet 数据
     * @return 是否消费
     */
    fun onHandleOutPacket(packet: T): Boolean

    /**
     * 需求处理的接收包
     * @param packet 数据
     * @return 是否消费
     */
    fun onHandleInPacket(packet: T): Boolean
}