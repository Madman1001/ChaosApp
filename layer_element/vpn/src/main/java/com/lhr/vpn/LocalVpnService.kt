package com.lhr.vpn

import android.net.VpnService
import android.util.Log

/**
 * @author lhr
 * @date 2021/11/9
 * @des 本地虚拟代理网络服务
 */
class LocalVpnService: VpnService() {
    companion object{
        private const val TAG = "LocalVpnService"
    }
    override fun onCreate() {
        super.onCreate()
        Log.e(TAG,"onCreate")
    }

}