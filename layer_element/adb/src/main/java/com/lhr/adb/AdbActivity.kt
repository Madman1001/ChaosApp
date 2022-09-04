package com.lhr.adb

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayoutMediator
import com.lhr.adb.databinding.ActivityAdbBinding
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.common.ui.BaseFragmentAdapter

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
@CElement(name = "命令行模块")
class AdbActivity : BaseActivity<ActivityAdbBinding>() {
    private val tag = "AS_${this::class.java.simpleName}"
    private var adapter: BaseFragmentAdapter<ConnectConfig>? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        adapter = BaseFragmentAdapter(this) { position, data ->
            if (position == (adapter?.itemCount ?: 0) - 1) {
                AdbConnectFragment()
            } else {
                AdbClientFragment.create(position, data.address, data.port)
            }
        }

        val viewModel = ViewModelProviders.of(this).get(ConnectViewModel::class.java)

        mBinding.viewpager.run {
            adapter = this@AdbActivity.adapter
            isUserInputEnabled = true
        }
        TabLayoutMediator(mBinding.tabLay, mBinding.viewpager) { tab, position ->
            tab.text = adapter?.getItem(position)?.name ?: ""
        }.attach()

        viewModel.notificationList.observe(this) {
            adapter?.replaceData(viewModel.connectList)
        }
        viewModel.connectList.add(ConnectConfig("...", "", 0))
        viewModel.notificationList.value = (viewModel.notificationList.value ?: 0) + 1
    }
}