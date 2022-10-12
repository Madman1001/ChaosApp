package com.lhr.vpn.handle

import android.net.VpnService
import com.lhr.vpn.pool.TCPProxyClientPool
import com.lhr.vpn.pool.UDPProxyClientPool
import com.lhr.vpn.protocol.IProtocol
import com.lhr.vpn.protocol.TCPPacket
import com.lhr.vpn.protocol.UDPPacket

/**
 * @author lhr
 * @date 2021/12/8
 * @des 运输层拦截器
 */
class TransportProxyHandle(private val vpnService: VpnService): VpnProxyHandle() {
    override fun onInput(data: IProtocol): IProtocol? {
        if (data is UDPPacket){
            UDPProxyClientPool.sendPacket(vpnService, this, data)
        }else if (data is TCPPacket){
            TCPProxyClientPool.sendPacket(vpnService,this, data)
        }
        return null
    }

    override fun onOutput(data: IProtocol): IProtocol {
        return data
    }
}