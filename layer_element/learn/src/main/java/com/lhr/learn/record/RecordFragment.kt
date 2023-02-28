package com.lhr.learn.record

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Context.USB_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.LearnActivity
import com.lhr.learn.databinding.FragmentRecordBinding

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description: 录音页面
 */
class RecordFragment : BaseFragment<FragmentRecordBinding>() {

    private val needPermissions = arrayOf<String>(Manifest.permission.RECORD_AUDIO)

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val proxyClick by lazy { ProxyClick() }

    private val viewModel by lazy { RecordViewModel() }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.click = proxyClick
        permissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                //权限请求结果
            }

        viewModel.vmIsRecording.observe(viewLifecycleOwner){
            mBinding.recordBtn.text = if (it == true) "STOP" else "START"

            mBinding.tv.text = "..."
        }

        viewModel.vmCurrentDbVal.observe(viewLifecycleOwner){
            mBinding.tv.text = "db value:$it"
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (!hasPermission(attachActivity)){
            permissionLauncher.launch(needPermissions)
        }
    }

    inner class ProxyClick{
        fun onRecord(){
            if (viewModel.isNoiseWatchRun){
                viewModel.stop()
            } else {
                viewModel.startNoiseWatch(attachActivity)
            }
        }
    }

    private fun hasPermission(context: Context): Boolean{
        return needPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}