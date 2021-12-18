package com.lhr.vpn.handle

import android.net.VpnService
import com.lhr.vpn.protocol.IProtocol

/**
 * @author lhr
 * @date 2021/12/18
 * @des 应用层接管
 */
class ApplicationProxyHandle(private val vpnService: VpnService): VpnProxyHandle()  {

    override fun onInput(data: IProtocol): IProtocol? {
        TODO("Not yet implemented")
    }

    override fun onOutput(data: IProtocol): IProtocol? {
        TODO("Not yet implemented")
    }
}