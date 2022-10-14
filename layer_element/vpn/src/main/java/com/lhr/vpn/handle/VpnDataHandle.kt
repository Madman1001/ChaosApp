package com.lhr.vpn.handle

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description:
 */
object VpnDataHandle: INetHandle<ByteArray> {
    override fun onHandle(packet: ByteArray): Boolean {
        return true
    }
}