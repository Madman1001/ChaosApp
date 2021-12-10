package com.lhr.vpn

import android.content.Intent
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.lhr.centre.annotation.CElement
import com.lhr.utils.NetworkUtils
import com.lhr.test.LocalVpnTest

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

        this.findViewById<TextView>(R.id.vpn_tv).text = "Local IP: ${NetworkUtils.getHostIp()}"
    }

    fun onClick(view: View){
        val address = this.findViewById<EditText>(R.id.test_ip_edit_text).text.toString()
        val port = this.findViewById<EditText>(R.id.test_port_edit_text).text.toString()
        when(view.id){
            R.id.start_vpn_bt ->{
                startVPN()
            }
            R.id.stop_vpn_bt ->{
                stopVPN()
            }
            R.id.udp_client_test_vpn_bt -> {
                if (address.isNotEmpty() || port.isNotEmpty()){
                    LocalVpnTest.udpClientTest(address,port.toInt())
                }
            }
            R.id.udp_server_test_vpn_bt -> {
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
        LocalVpnService.vpnService.get()?.stopSelf()
    }
}