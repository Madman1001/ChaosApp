package com.lhr.vpn.handle

import android.net.VpnService
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.IProtocol
import com.lhr.vpn.protocol.UDPPacket

/**
 * @author lhr
 * @date 2021/12/8
 * @des 网络层接管
 */
class NetworkProxyHandle(private val vpnService: VpnService): VpnProxyHandle() {

    override fun onInput(data: IProtocol): IProtocol? {
        if (data is IPPacket){
            if (data.isUdp()){
                return UDPPacket(data)
            }
        }
        return null
    }

    override fun onOutput(data: IProtocol): IProtocol {
        return data
    }
}