package com.lhr.vpn

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.common.utils.NetworkUtils
import com.lhr.test.LocalVpnTest
import com.lhr.vpn.databinding.ActivityLocalVpnBinding

/**
 * @author lhr
 * @date 2021/11/9
 * @des
 */
@CElement(name = "网络代理")
class LocalVpnActivity : BaseActivity<ActivityLocalVpnBinding>() {

    companion object {
        private const val VPN_REQUEST_CODE = 0xF0F

        private const val VPN_REQUEST_KEY = "VPN_REQUEST_KEY"

        init {
            System.loadLibrary("chaos_vpn")
        }
    }

    /**
     * 获取权限资源
     */
    private lateinit var _launchActivity: ActivityResultLauncher<Intent>

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        _launchActivity = this.activityResultRegistry.register(
            VPN_REQUEST_KEY,
            this,
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> {
                if (RESULT_OK == it.resultCode){
                    startService(Intent(this, LocalVpnService::class.java))
                }
            })
        mBinding.vpnEditText.setText("Local IP: ${NetworkUtils.getHostIp()}")
    }

    fun onClick(view: View){
        val address = mBinding.testIpEditText.text.toString()
        val port = mBinding.testPortEditText.text.toString()
        val data = mBinding.vpnEditText.text.toString()
        when(view.id){
            R.id.start_vpn_bt ->{
                startVPN()
            }
            R.id.stop_vpn_bt ->{
                stopVPN()
            }
            R.id.udp_client_test_vpn_bt -> {
                if (address.isNotEmpty() || port.isNotEmpty()){
                    LocalVpnTest.udpClientTest(address, port.toInt(), data)
                }
            }
            R.id.udp_server_test_vpn_bt -> {
                LocalVpnTest.udpServerTest()
            }
            R.id.tcp_client_test_vpn_bt -> {
                if (address.isNotEmpty() || port.isNotEmpty()){
                    LocalVpnTest.tcpClientTest(address, port.toInt(), data)
                }
            }
            R.id.tcp_server_test_vpn_bt -> {
                LocalVpnTest.tcpServerTest()
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