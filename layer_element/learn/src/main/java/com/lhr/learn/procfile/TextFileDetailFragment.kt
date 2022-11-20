package com.lhr.learn.procfile

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
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
class TextFileDetailFragment : BaseFragment<FragmentTextFileDetailBinding>() {
    private var filePath = ""
    val liveTitle = MutableLiveData<String>("")

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        mBinding.fragment = this
        filePath = arguments?.getString(KEY_FILE_PATH) ?: ""
        liveTitle.value = filePath
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

    fun onWarp(){
        mBinding.fileDetailTv.run {
            val parentView = parent as? ViewGroup ?: return
            val tvContainer = this@TextFileDetailFragment.mBinding.textViewContainer
            parentView.removeView(this)
            if (parentView === tvContainer){
                mBinding.hSrcollView.addView(this)
            } else {
                mBinding.textViewContainer.addView(this)
            }
        }
    }

    companion object {
        private const val KEY_FILE_PATH = "KEY_FILE_PATH"

        fun start(activity: Activity, path: String) {
            activity.startFragment<TextFileDetailFragment>(Bundle().apply {
                putString(
                    KEY_FILE_PATH,
                    path
                )
            })
        }
    }
}