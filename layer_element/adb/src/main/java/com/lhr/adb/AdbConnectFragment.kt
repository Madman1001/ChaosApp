package com.lhr.adb

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.lhr.adb.databinding.FragmentAdbConnectBinding
import com.lhr.common.ext.initInputBar
import com.lhr.common.ext.isValidInet4Address
import com.lhr.common.ui.BaseFragment

/**
 * @author lhr
 * @date 4/9/2022
 * @des adb connect fragment
 */
class AdbConnectFragment:BaseFragment<FragmentAdbConnectBinding>() {
    private lateinit var viewModel: ConnectViewModel

    private val list: ArrayList<ConnectConfig>
        get() = viewModel.connectList

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.pager = this

        mBinding.ipEditText.initInputBar(mBinding.root as ViewGroup)

        mBinding.portEditText.initInputBar(mBinding.root as ViewGroup)

        viewModel = ViewModelProviders.of(attachActivity).get(ConnectViewModel::class.java)
    }

    fun connectADBD(){
        val address = mBinding.ipEditText.text.toString()
        var port = -1

        kotlin.runCatching {
            port = mBinding.portEditText.text.toString().toInt()
        }

        if (!address.isValidInet4Address()){
            Toast.makeText(attachActivity, "address is invalid ", Toast.LENGTH_SHORT).show()
            return
        }

        if (port <= 0){
            Toast.makeText(attachActivity, "port is invalid ", Toast.LENGTH_SHORT).show()
            return
        }

        list.add(list.size - 1, ConnectConfig("window${list.size}", address, port))
        viewModel.notificationList.value = (viewModel.notificationList.value ?: 0) + 1
    }

}