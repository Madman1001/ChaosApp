package com.lhr.vpn.test

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author lhr
 * @date 2021/11/14
 * @des 测试样例
 */
object LocalVpnTest {
    const val tag = "VpnTest"
    fun httpTest(){
        Thread({
            val httpTestUrl = URL("http://www.baidu.com")
            val http = httpTestUrl.openConnection() as HttpURLConnection
            http.connect()
            Log.d(tag,"start http connect")

            Thread.sleep(5000)

            http.disconnect()
            Log.d(tag,"start http disconnect")

        }, "${tag}:httpTest").start()
    }
}