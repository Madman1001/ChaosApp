package com.lhr.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import java.lang.Exception
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * @author lhr
 * @date 2021/12/6
 * @des 网络信息获取工具
 */
object NetworkUtils {

    /**
     * 获取网络ip地址
     */
    @SuppressLint("MissingPermission")
    fun getIpAddress(context: Context): String {
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                val netCap: NetworkCapabilities? = connManager.getNetworkCapabilities(connManager.activeNetwork)
                if (netCap != null){
                    if (netCap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return getHostIp()
                    } else if (netCap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return getHostIp()
                    }  else if (netCap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return getHostIp()
                    }
                }
            }else{
                val netInfo: NetworkInfo? = connManager.activeNetworkInfo
                if (netInfo != null && netInfo.isConnected){
                    when (netInfo.type){
                        ConnectivityManager.TYPE_MOBILE -> return getHostIp()
                        ConnectivityManager.TYPE_WIFI -> return getHostIp()
                        ConnectivityManager.TYPE_ETHERNET -> return getHostIp()
                    }
                }
            }
        }catch (e: Exception){
            return ""
        }
        return ""
    }

    fun getHostIp(): String{
        var hostIp = ""
        try {
            val nis = NetworkInterface.getNetworkInterfaces()
            var ia:InetAddress? = null
            while (nis.hasMoreElements()){
                val ni = nis.nextElement() as NetworkInterface
                val ias = ni.inetAddresses
                while (ias.hasMoreElements()){
                    ia = ias.nextElement()
                    if (ia is Inet6Address){
                        continue
                    }
                    val ip = ia?.hostAddress
                    if (ip != null && ip != "127.0.0.1"){
                        hostIp = ip
                        break
                    }
                }
            }
        }catch (e: Exception){

        }
        return hostIp
    }
}