package com.lhr.vpn

import android.app.PendingIntent
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
import java.util.concurrent.atomic.AtomicInteger
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
    }

    private val connectionThread = AtomicReference<Thread>()

    private val vpnConnection = AtomicReference<LocalVpnConnection>()

    private val nextConnectionId = AtomicInteger(1)

    private lateinit var mPendingIntent: PendingIntent

    init {
        System.loadLibrary("chaos_vpn")
    }

    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate")
        mPendingIntent = PendingIntent.getActivity(this,0,Intent(this,LocalVpnActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT)
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
            startConnect(LocalVpnConnection(this,tunInterface))
        }
    }

    private fun startConnect(connection: LocalVpnConnection){
        val thread =
            Thread(connection, PROXY_SESSION_NAME + nextConnectionId.getAndIncrement().toString())
        setConnection(connection)
        setConnectionThread(thread)
        thread.start()
    }

    private fun setConnection(connection: LocalVpnConnection?){
        vpnConnection.getAndSet(connection)?.close()
    }

    private fun setConnectionThread(thread: Thread?){
        connectionThread.getAndSet(thread)?.interrupt()
    }

    private fun disconnect() {
        setConnectionThread(null)
        setConnection(null)
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