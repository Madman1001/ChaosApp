package com.lhr.learn.procfile

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.lhr.common.ui.BaseFragment
import com.lhr.common.ui.startFragment
import com.lhr.learn.databinding.FragmentTextFileDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class TxtFileDetailFragment : BaseFragment<FragmentTextFileDetailBinding>() {
    private var filePath = ""

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        filePath = arguments?.getString(KEY_FILE_PATH) ?: ""

        kotlin.runCatching {
            val file = File(filePath)
            if (file.exists() && file.isFile) {
                lifecycleScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        val text = file.inputStream().bufferedReader().readText()
                        withContext(Dispatchers.Main) {
                            mBinding.fileDetailTv.text = text
                        }
                    }.onFailure {
                        attachActivity.finish()
                    }
                }
            } else {
                attachActivity.finish()
            }
        }.onFailure {
            attachActivity.finish()
        }
    }

    companion object {
        private const val KEY_FILE_PATH = "KEY_FILE_PATH"

        fun start(activity: Activity, path: String) {
            activity.startFragment<TxtFileDetailFragment>(Bundle().apply {
                putString(
                    KEY_FILE_PATH,
                    path
                )
            })
        }
    }
}