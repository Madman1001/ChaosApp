package com.lhr.vpn.handle

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

    fun setNextHandle(handle: VpnProxyHandle?){
        chain.nextHandle = handle
        handle?.chain?.preHandle = this
    }
}