package com.lhr.vpn.socks.socks

/**
 * @author lhr
 * @date 15/10/2022
 * @des 中转接口
 */
interface ITunSocket<Recv,Send> {
    fun tunToSocks(send: Send)

    fun socksToTun(recv: Recv)
}