package com.lhr.vpn.ext

import com.lhr.common.utils.NetworkUtils
import com.lhr.vpn.LocalVpnConfig
import com.lhr.vpn.socks.net.v4.NetIpPacket

/**
 * @author lhr
 * @date 22/10/2022
 * @des packet数据扩展工具
 */
private val hostIp = NetworkUtils.getHostIp()
private val vpnIp = LocalVpnConfig.PROXY_ADDRESS
/**
 * 是否是从外部发往内部的ip数据包
 */
fun NetIpPacket.isOutsideToInside(): Boolean{
    val sourceAddress = this.sourceAddress.hostAddress
    val targetAddress = this.targetAddress.hostAddress
    val isLocal = targetAddress == hostIp || targetAddress == vpnIp
    val isRemote = sourceAddress != hostIp && sourceAddress != vpnIp
    return isLocal and isRemote
}

/**
 * 是否是从内部发往外部的ip数据包
 */
fun NetIpPacket.isInsideToOutside(): Boolean{
    val sourceAddress = this.sourceAddress.hostAddress
    val targetAddress = this.targetAddress.hostAddress
    val isLocal = sourceAddress == hostIp || sourceAddress ==vpnIp
    val isRemote = targetAddress != hostIp && targetAddress != vpnIp
    return isLocal and isRemote
}