package com.lhr.vpn.socks.channel

import java.util.concurrent.LinkedBlockingDeque

/**
 * @author lhr
 * @date 29/10/2022
 * @des tun channel
 */
abstract class BaseTunChannel<In, Out> {
    protected val sendPool: LinkedBlockingDeque<Out> by lazy { LinkedBlockingDeque() }

    protected val receivePool: LinkedBlockingDeque<In> by lazy { LinkedBlockingDeque() }

    fun send(data: Out) {
        sendPool.offerLast(data)
    }

    fun receive(): In {
        return receivePool.takeFirst()
    }

    companion object{
        val CloseSign = byteArrayOf()
    }
}