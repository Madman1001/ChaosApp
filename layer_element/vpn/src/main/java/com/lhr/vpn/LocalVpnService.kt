package com.lhr.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.lhr.vpn.LocalVpnConfig.PROXY_ADDRESS
import com.lhr.vpn.LocalVpnConfig.PROXY_PORT
import com.lhr.vpn.LocalVpnConfig.PROXY_ROUTE_ADDRESS
import com.lhr.vpn.LocalVpnConfig.PROXY_ROUTE_PORT
import com.lhr.vpn.LocalVpnConfig.PROXY_SESSION_NAME
import com.lhr.vpn.socks.Tun2Socks
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

/**
 * @author lhr
 * @date 2021/11/9
 * @des 本地虚拟代理网络服务
 */
class LocalVpnService : VpnService() {
    companion object {
        private const val TAG = "LocalVpnService"

        const val VPN_CONTROL_ACTION_START = "VPN_START"

        const val VPN_CONTROL_ACTION_STOP = "VPN_STOP"

        var vpnService: WeakReference<VpnService?> = WeakReference(null)
        fun startVPN(context: Context): Boolean{
            val intent = VpnService.prepare(context)
            if (intent != null){
                return false
            } else {
                context.startService(Intent(context, LocalVpnService::class.java))
                return true
            }
        }

        fun stopVPN(context: Context) {
            val stopIntent = Intent().apply {
                action = VPN_CONTROL_ACTION_STOP
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(stopIntent)
        }
    }

    private val vpnConnection = AtomicReference<Tun2Socks>()

    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action){
                VPN_CONTROL_ACTION_STOP -> {
                    vpnConnection.get()?.stopProxy()
                    this@LocalVpnService.stopSelf()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        vpnService = WeakReference(this)
        Log.e(TAG, "onCreate")
        connect()
        registerControl()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterControl()
        disconnect()
    }

    private fun connect() {
        val builder = Builder()
        val tunInterface = builder.setSession(PROXY_SESSION_NAME)
            .addAddress(PROXY_ADDRESS, PROXY_PORT)
            .addRoute(PROXY_ROUTE_ADDRESS, PROXY_ROUTE_PORT)
            //暂时只代理自身网络
            .addAllowedApplication(this.packageName)
            //创建vpn通道，开始代理网络
            .establish()
        if (tunInterface != null){
            vpnConnection.set(Tun2Socks(tunInterface, this))
            vpnConnection.get()?.startProxy()
        }
    }

    private fun disconnect() {
        vpnConnection.get()?.stopProxy()
        vpnConnection.set(null)
    }

    private fun registerControl() {
        val filter = IntentFilter()
        filter.addAction(VPN_CONTROL_ACTION_START)
        filter.addAction(VPN_CONTROL_ACTION_STOP)
        LocalBroadcastManager.getInstance(this).registerReceiver(controlReceiver, filter)
    }

    private fun unregisterControl() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(controlReceiver)
    }
}