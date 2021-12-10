package com.lhr.vpn.handle

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.protocol.IProtocol
import com.lhr.vpn.protocol.UDPPacket

/**
 * @author lhr
 * @date 2021/12/8
 * @des 网络层拦截器
 */
class NetworkProxyHandle(vpnService: VpnService): VpnProxyHandle() {

    override fun inputData(data: IProtocol) {
        Log.d(tag,"inputData")
        if (data is IPPacket){
            if (data.isUdp()){
                chain.nextHandle?.inputData(UDPPacket(data))
            }
        }
    }

    override fun outputData(data: IProtocol) {
        Log.d(tag,"outputData")
        chain.preHandle?.outputData(data)
    }
}