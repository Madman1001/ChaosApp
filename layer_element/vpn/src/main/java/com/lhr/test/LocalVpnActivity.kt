package com.lhr.test

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.common.ui.BaseAdapter
import com.lhr.common.utils.NetworkUtils
import com.lhr.vpn.LocalVpnService
import com.lhr.vpn.R
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
    }

    data class TestData(val name: String,val action: ()->Unit)

    /**
     * 获取权限资源
     */
    private lateinit var _launchActivity: ActivityResultLauncher<Intent>

    private val testDataAdapter = object : BaseAdapter<TestData>() {
        override fun bind(holder: ViewHolder, position: Int, data: TestData) {
            holder.itemView.findViewById<TextView>(R.id.itemBtn).run {
                text = data.name
                setOnClickListener {
                    data.action.invoke()
                }
            }
        }
        override var layout: Int = R.layout.item_test_layout
    }

    private val dataList = listOf(
        TestData("开启VPN"){startVPN()},
        TestData("关闭VPN"){stopVPN()},
        TestData("UDP测试"){udpClientTest()},
        TestData("TCP测试"){tcpClientTest()},
        TestData("Http测试"){httpClientTest()},
        TestData("Https测试"){httpsClientTest()},
    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        LocalVpnTest.initManager(this.application)
        LocalVpnTest.udpServerTest()

        _launchActivity = this.activityResultRegistry.register(
            VPN_REQUEST_KEY,
            this,
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> {
                if (RESULT_OK == it.resultCode){
                    startVPN()
                }
            })
        mBinding.vpnEditText.setText("Local IP: ${NetworkUtils.getHostIp()}")
        mBinding.testRv.run {
            adapter = testDataAdapter
            layoutManager = GridLayoutManager(context, 2).apply {
                this.orientation = LinearLayoutManager.VERTICAL
            }
        }
        testDataAdapter.replaceData(dataList)

        mBinding.testIpEditText.setText(LocalVpnTest.getTestIp())
    }


    private fun udpClientTest(){
        val address = mBinding.testIpEditText.text.toString()
        val port = mBinding.testPortEditText.text.toString()
        val data = mBinding.vpnEditText.text.toString()
        if (address.isNotEmpty() || port.isNotEmpty()){
            LocalVpnTest.udpClientTest(address, port.toInt(), data)
        }

    }

    private fun tcpClientTest(){
        val address = mBinding.testIpEditText.text.toString()
        val port = mBinding.testPortEditText.text.toString()
        val data = mBinding.vpnEditText.text.toString()
        if (address.isNotEmpty() || port.isNotEmpty()){
            LocalVpnTest.tcpClientTest(address, port.toInt(), data)
        }
    }

    private fun httpClientTest(){
        LocalVpnTest.httpTest()
    }

    private fun httpsClientTest(){
        LocalVpnTest.httpTest()
    }

    private fun startVPN() {
        if (!LocalVpnService.startVPN(this)){
            val intent = VpnService.prepare(this)
            if (intent != null){
                _launchActivity.launch(intent)
            }
        }
    }

    private fun stopVPN() {
        LocalVpnService.stopVPN(this)
    }
}