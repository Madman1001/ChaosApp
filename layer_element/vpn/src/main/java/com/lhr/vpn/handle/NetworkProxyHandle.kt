package com.lhr.vpn.handle

import com.lhr.vpn.protocol.IProtocol

/**
 * @author lhr
 * @date 2021/12/8
 * @des 网络层拦截器
 */
class NetworkProxyHandle(): IHandle {

    override fun inputHandle(data: IProtocol): IProtocol {
        TODO("Not yet implemented")
    }

    override fun outputHandle(data: IProtocol): IProtocol {
        TODO("Not yet implemented")
    }
}