package com.lhr.learn.procfile

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.databinding.FragmentProcFileDetailBinding
import com.lhr.learn.startFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class ProcFileDetailFragment: BaseFragment<FragmentProcFileDetailBinding>() {
    private var filePath = ""

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        filePath = arguments?.getString(KEY_FILE_PATH) ?: ""
        val file = File(filePath)
        if (file.exists() && file.isFile){
            lifecycleScope.launch(Dispatchers.IO){
                kotlin.runCatching {
                    val text = file.inputStream().bufferedReader().readText()
                    withContext(Dispatchers.Main){
                        mBinding.fileDetailTv.text = text
                    }
                }.onFailure {
                    Toast.makeText(requireContext(), "缺少相关权限", Toast.LENGTH_SHORT).show()
                    attachActivity.finish()
                }
            }
        } else {
            Toast.makeText(requireContext(), "无法打开文件", Toast.LENGTH_SHORT).show()
            attachActivity.finish()
        }
    }

    companion object{
        private const val KEY_FILE_PATH = "KEY_FILE_PATH"

        fun start(activity: Activity, path: String){
            activity.startFragment<ProcFileDetailFragment>(Bundle().apply { putString(KEY_FILE_PATH, path)})
        }
    }
}