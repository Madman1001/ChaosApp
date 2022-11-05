package com.lhr.vpn

/**
 * @author lhr
 * @date 2021/11/13
 * @des 配置信息
 */
class LocalVpnConfig {
    companion object{
        //代理服务名称
        const val PROXY_SESSION_NAME = "ChaosVPN"

        //代理的主机地址，暂时只支持 ipv4
        const val PROXY_ADDRESS = "192.168.2.2"

        //代理的主机端口
        const val PROXY_PORT = 32

        //允许通过的路由地址
        const val PROXY_ROUTE_ADDRESS = "0.0.0.0"

        //允许通过的路由端口
        const val PROXY_ROUTE_PORT = 0

        const val PROXY_DNS_SERVER = "8.8.8.8"

        const val PROXY_MTU = 255

        const val PROXY_TUN_IS_BLOCKING = true

        val HostIp = PROXY_ADDRESS.toIpInt()
    }

    var sessionName = PROXY_SESSION_NAME
    var address = PROXY_ADDRESS
    var port = PROXY_PORT
    var routeAddress = PROXY_ROUTE_ADDRESS
    var routePort = PROXY_ROUTE_PORT
    var dnsServerAddress = PROXY_DNS_SERVER
    var mtu = PROXY_MTU
    var isBlocking = PROXY_TUN_IS_BLOCKING
}