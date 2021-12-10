package com.lhr.vpn.handle

import com.lhr.vpn.protocol.IProtocol

/**
 * @author lhr
 * @date 2021/12/8
 * @des 双向拦截器，转发和接收
 */
interface IHandle {

    interface Chain{
        var nextHandle: IHandle?

        var preHandle: IHandle?
    }

    /**
     * 输入事件的拦截方法
     * @param data 输入数据
     */
    fun inputHandle(data: IProtocol): IProtocol

    /**
     * 输出事件的拦截方法
     * @param data 输出数据
     */
    fun outputHandle(data: IProtocol): IProtocol
}