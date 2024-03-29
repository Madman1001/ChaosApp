package com.lhr.test

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.common.ui.BaseAdapter
import com.lhr.common.utils.NetworkUtils
import com.lhr.vpn.LocalVpnService
import com.lhr.vpn.R
import com.lhr.vpn.databinding.ActivityLocalVpnBinding
import kotlinx.coroutines.launch


/**
 * @author lhr
 * @date 2021/11/9
 * @des
 */
@CElement(name = "网络代理")
class LocalVpnActivity : BaseActivity<ActivityLocalVpnBinding>() {

    companion object {

        private const val VPN_REQUEST_KEY = "VPN_REQUEST_KEY"
    }

    data class TestData(val name: String,val action: ()->Unit)

    /**
     * 获取权限资源
     */
    private lateinit var _launchActivity: ActivityResultLauncher<Intent>
    private lateinit var mWebView: WebView

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
        TestData("UDP Server测试"){udpServerTest()},
        TestData("TCP Server测试"){tcpServerTest()},
        TestData("Http测试"){httpClientTest()},
        TestData("Https测试"){httpsClientTest()},
        TestData("加载百度"){loadBaidu()},

    )

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        LocalVpnTest.initManager(this.application)

        _launchActivity = this.activityResultRegistry.register(
            VPN_REQUEST_KEY,
            this,
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> {
                if (RESULT_OK == it.resultCode){
                    startVPN()
                }
            })
        mBinding.testRv.run {
            adapter = testDataAdapter
            layoutManager = GridLayoutManager(context, 2).apply {
                this.orientation = LinearLayoutManager.VERTICAL
            }
        }
        testDataAdapter.replaceData(dataList)

        mBinding.testIpEditText.setText(LocalVpnTest.getTestIp())

        mBinding.webViewContainer.run {
            mWebView = WebView(context).apply {
                val dp = this@LocalVpnActivity.resources.configuration.densityDpi
                if(dp > 650) {
                    setInitialScale(95)
                }else if(dp > 520) {
                    setInitialScale(80)
                }else if(dp > 450) {
                    setInitialScale(70)
                }else if(dp > 300) {
                    setInitialScale(60)
                }else {
                    setInitialScale(50)
                }
            }

            mWebView.settings.apply {
                cacheMode = LOAD_NO_CACHE
                useWideViewPort = true
                loadWithOverviewMode = true
                displayZoomControls = false
                builtInZoomControls = true
                setSupportZoom(true)
                javaScriptEnabled = true
            }
            mWebView.webViewClient = object: WebViewClient(){
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    lifecycleScope.launch {
                        view.loadUrl(url)
                    }
                    return false
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    lifecycleScope.launch {
                        view.loadUrl(request.url.toString())
                    }
                    return false
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    view.loadUrl("""
                        javascript:(function(){
                            var objs = document.getElementsByTagName('img'); 
                            for(var i=0;i<objs.length;i++)
                            {
                                var img = objs[i];
                                img.style.maxWidth = '100%'; img.style.height = 'auto';
                            }
                        })()
                    """.trimIndent())
                }
            }
            val param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            addView(mWebView, param)
        }
    }


    private fun udpClientTest(){
        val address = mBinding.testIpEditText.text.toString()
        val port = mBinding.testPortEditText.text.toString()
        val data = "Local IP: ${NetworkUtils.getHostIp()}, Udp Client Test"
        if (address.isNotEmpty() || port.isNotEmpty()){
            LocalVpnTest.udpClientTest(address, port.toInt(), data)
        }

    }

    private fun tcpClientTest(){
        val address = mBinding.testIpEditText.text.toString()
        val port = mBinding.testPortEditText.text.toString()
        val data = "Local IP: ${NetworkUtils.getHostIp()}, Tcp Client Test"
        if (address.isNotEmpty() || port.isNotEmpty()){
            LocalVpnTest.tcpClientTest(address, port.toInt(), data)
        }
    }

    private fun udpServerTest(){
        LocalVpnTest.udpServerTest()
    }

    private fun tcpServerTest(){
        LocalVpnTest.tcpServerTest()
    }

    private fun httpClientTest(){
//        LocalVpnTest.httpTest()
        mWebView.loadUrl("http://img.xjh.me/random_img.php")
    }

    private fun httpsClientTest(){
//        LocalVpnTest.httpsTest()
        mWebView.loadUrl("https://img.xjh.me/random_img.php")
    }

    private fun loadBaidu(){
        mWebView.loadUrl("http://www.baidu.com")
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