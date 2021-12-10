package com.lhr.vpn.proxy

/**
 * @author lhr
 * @date 2021/12/17
 * @des 代理绑定接口
 */
interface IProxyBind {

    enum class BindStatus {
        UNBOUND, BOUND, UNKNOWN
    }

    fun getStatus(): BindStatus

    /**
     * 绑定代理端口
     * @param port 被代理端口
     */
    fun bind(port: Int)

    /**
     * 解绑代理端口
     */
    fun unbind()
}