package com.lhr.vpn

import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.lhr.centre.annotation.CElement
import com.lhr.vpn.test.LocalVpnTest
import junit.framework.Test

/**
 * @author lhr
 * @date 2021/11/9
 * @des
 */
@CElement(name = "网络代理")
class LocalVpnActivity : AppCompatActivity() {

    companion object {
        private const val VPN_REQUEST_CODE = 0xF0F

        private const val VPN_REQUEST_KEY = "VPN_REQUEST_KEY"
    }

    /**
     * 获取权限资源
     */
    private lateinit var _launchActivity: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_vpn)
        _launchActivity = this.activityResultRegistry.register(
            VPN_REQUEST_KEY,
            this,
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> {
                if (RESULT_OK == it.resultCode){
                    startService(Intent(this, LocalVpnService::class.java))
                }
            })
    }

    fun onClick(view: View){
        when(view.id){
            R.id.start_vpn_bt ->{
                startVPN()
                this.findViewById<TextView>(R.id.vpn_tv).text = "VPN START"
            }
            R.id.stop_vpn_bt ->{
                stopVPN()
                this.findViewById<TextView>(R.id.vpn_tv).text = "VPN STOP"
            }
            R.id.udp_client_test_vpn_bt -> {
                LocalVpnTest.udpClientTest()
                this.findViewById<TextView>(R.id.vpn_tv).text = "VPN UDP CLIENT TEST"
            }
            R.id.udp_server_test_vpn_bt -> {
                LocalVpnTest.udpServerTest()
                this.findViewById<TextView>(R.id.vpn_tv).text = "VPN UDP SERVER TEST"
            }
            R.id.tcp_client_test_vpn_bt -> {
            }
            R.id.tcp_server_test_vpn_bt -> {
            }
        }
    }

    private fun startVPN() {
        val intent = VpnService.prepare(this)
        if (intent != null){
            _launchActivity.launch(intent)
        } else {
            startService(Intent(this, LocalVpnService::class.java))
        }
    }

    private fun stopVPN() {
        stopService(Intent(this,LocalVpnService::class.java))
    }
}