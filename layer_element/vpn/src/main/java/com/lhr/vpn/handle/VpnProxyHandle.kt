package com.lhr.vpn.handle

import com.lhr.vpn.protocol.IProtocol

/**
 * @author lhr
 * @date 2021/12/8
 * @des tun虚拟网卡接管
 */
abstract class VpnProxyHandle: IProxyTun {
    protected val tag = this::class.java.simpleName

    internal class Chain {
        var nextHandle: IProxyTun? = null

        var preHandle: IProxyTun? = null
    }

    internal var chain: Chain = Chain()

    fun addHandle(handle: VpnProxyHandle?){
        chain.nextHandle = handle
        handle?.chain?.preHandle = this
    }

    final override fun inputData(data: IProtocol) {
        onInput(data)?.let {
            chain.nextHandle?.inputData(it)
        }
    }

    final override fun outputData(data: IProtocol) {
        onOutput(data)?.let {
            chain.preHandle?.outputData(it)
        }
    }

    abstract fun onInput(data: IProtocol): IProtocol?

    abstract fun onOutput(data: IProtocol): IProtocol?
}