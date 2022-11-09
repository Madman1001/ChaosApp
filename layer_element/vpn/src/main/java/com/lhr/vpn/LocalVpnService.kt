package com.lhr.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.lhr.vpn.socks.TunSocks
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

        var config: LocalVpnConfig = LocalVpnConfig()

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

    private val vpnConnection = AtomicReference<TunSocks>()

    lateinit var tunInterface: ParcelFileDescriptor
        private set
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
        tunInterface = builder.setSession(config.sessionName)
            .addAddress(config.address, config.port)
            .addRoute(config.routeAddress, config.routePort)
            //设置dns服务器
            //.addDnsServer(config.dnsServerAddress)
            //暂时只代理自身网络
            .addAllowedApplication(this.packageName)
            //设置流量计费
            //.setMetered(false)
            //设置配置界面的activity
            //.setConfigureIntent(config.configureIntent)
            //设置ip的mtu
            .setMtu(config.mtu)
            //.allowFamily(OsConstants.AF_INET)
            .setBlocking(config.isBlocking)
            //创建vpn通道，开始代理网络
            .establish()!!
        vpnConnection.set(TunSocks(this))
        vpnConnection.get()?.startProxy()
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