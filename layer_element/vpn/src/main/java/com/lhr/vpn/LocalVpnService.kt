package com.lhr.vpn

import android.app.PendingIntent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlin.system.exitProcess

/**
 * @author lhr
 * @date 2021/11/9
 * @des 本地虚拟代理网络服务
 */
class LocalVpnService: VpnService() {
    companion object{
        private const val TAG = "LocalVpnService"
    }

    /**
     * VPN操作接口文件，
     * The interface works on IP packets, and a file descriptor is returned for the application to access them.
     */
    private var vpnInterface: ParcelFileDescriptor? = null

    private var pendingIntent: PendingIntent? = null

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate")

        setupVPN()
    }

    private fun setupVPN(){
        try {
            if (vpnInterface == null) {
                val builder: Builder = Builder()
                builder.addAddress(VpnConfig.VPN_ADDRESS, 32)
                builder.addRoute(VpnConfig.VPN_ROUTE, 0)
                builder.addDnsServer(VpnConfig.DNS)

                when(VpnConfig.CAPTURE_TYPE){
                    VpnConfig.VPNType.CURRENT_APP ->{
                        builder.addAllowedApplication(this.packageName)
                    }
                    VpnConfig.VPNType.DISABLE_SOME_APPS ->{
                        //TODO VpnService.addDisallowedApplication(packName: String)
                    }
                    VpnConfig.VPNType.ENABLE_SOME_APPS ->{
                        //TODO VpnService.addAllowedApplication(packName: String)
                    }
                    VpnConfig.VPNType.ALL_APPS ->{
                        //TODO
                    }
                    VpnConfig.VPNType.PASS_ALL_APPS ->{
                        //TODO VpnService.allowBypass()
                    }
                }
                vpnInterface = builder.setSession(getString(R.string.vpn_name))
//                    .setConfigureIntent(pendingIntent)
                    .establish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "error", e)
            exitProcess(0)
        }
    }
}