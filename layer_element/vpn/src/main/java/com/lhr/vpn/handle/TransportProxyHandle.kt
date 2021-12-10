package com.lhr.vpn.handle

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.pool.UDPProxyClientPool
import com.lhr.vpn.protocol.IProtocol
import com.lhr.vpn.protocol.UDPPacket

/**
 * @author lhr
 * @date 2021/12/8
 * @des 运输层拦截器
 */
class TransportProxyHandle(private val vpnService: VpnService): VpnProxyHandle() {

    override fun inputData(data: IProtocol) {
        Log.d(tag,"inputData")
        if (data is UDPPacket){
            UDPProxyClientPool.sendPacket(vpnService, this, data)
        }
    }

    override fun outputData(data: IProtocol) {
        Log.d(tag,"outputData")
        chain.preHandle?.outputData(data)
    }
}