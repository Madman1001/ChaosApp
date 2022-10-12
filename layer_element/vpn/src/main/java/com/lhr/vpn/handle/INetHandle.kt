package com.lhr.vpn.handle

/**
 * @author lhr
 * @date 13/10/2022
 * @des 数据包处理接口
 */
interface INetHandle<T> {
    fun onHandle(packet: T): Boolean
}