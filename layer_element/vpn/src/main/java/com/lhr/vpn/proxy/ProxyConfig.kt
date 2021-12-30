package com.lhr.vpn.proxy

/**
 * @author lhr
 * @date 2021/12/29
 * @des 代理所需的数据
 * @param sourceAddress 被代理的地址，默认为vpn地址
 * @param sourcePort 被代理的端口
 * @param targetAddress 目标地址
 * @param targetPort 目标端口
 */
data class ProxyConfig(
    val sourceAddress: String,
    val sourcePort: Int,
    val targetAddress: String,
    val targetPort: Int)
