package com.lhr.learn.classcheck

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.databinding.FragmentClassDetailBinding
import com.lhr.learn.startFragment

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class ClassDetailFragment : BaseFragment<FragmentClassDetailBinding>() {
    private var targetClassName = ""

    override fun initView(savedInstanceState: Bundle?) {
        targetClassName = arguments?.getString(KEY_CLASS_NAME) ?: ""
        if (!TextUtils.isEmpty(targetClassName)) {
            kotlin.runCatching {
                val clazz = Class.forName(targetClassName, false, attachActivity.classLoader)
                mBinding.classDetailTv.text = ClassBean(clazz).toString()
            }.onFailure {
                mBinding.classDetailTv.text = "\n\n\n\n 未找到类：$targetClassName"
            }
        } else {
            mBinding.classDetailTv.text = "\n\n\n\n 类名不能为空"
        }
    }

    companion object {
        private const val KEY_CLASS_NAME = "KEY_CLASS_NAME"

        fun start(activity: Activity, targetName: String) {
            activity.startFragment<ClassDetailFragment>(Bundle().apply {
                putString(
                    KEY_CLASS_NAME,
                    targetName
                )
            })
        }
    }
}