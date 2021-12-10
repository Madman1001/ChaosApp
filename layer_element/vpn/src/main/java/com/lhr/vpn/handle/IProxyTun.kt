package com.lhr.vpn.handle

import com.lhr.vpn.protocol.IProtocol

/**
 * @author lhr
 * @date 2021/12/11
 * @des 网络代理接口
 */
interface IProxyTun {

    /**
     * 输入事件的拦截方法
     * @param data 输入数据
     */
    fun inputData(data: IProtocol)

    /**
     * 输出事件的拦截方法
     * @param data 输出数据
     */
    fun outputData(data: IProtocol)
}