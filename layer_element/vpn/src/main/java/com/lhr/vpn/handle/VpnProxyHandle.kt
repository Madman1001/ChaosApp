package com.lhr.vpn.handle

import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.lhr.vpn.protocol.IProtocol

/**
 * @author lhr
 * @date 2021/12/8
 * @des tun虚拟网卡接管
 */
class VpnProxyHandle(
    private val vpnService: VpnService,
    private val tunInterface: ParcelFileDescriptor
): IHandle {

    override fun inputHandle(data: IProtocol): IProtocol {
        TODO("Not yet implemented")
    }

    override fun outputHandle(data: IProtocol): IProtocol {
        TODO("Not yet implemented")
    }
}