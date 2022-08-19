package com.lhr.common.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.lhr.common.ext.createViewBinding
import com.lhr.common.ext.obtainBindingClassByParadigm

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
abstract class BaseFragment<out DB: ViewDataBinding>: Fragment() {
    private var _bind: DB? = null

    val mBinding: DB get() = _bind!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dbClass = obtainBindingClassByParadigm(this) as Class<DB>
        _bind = viewLifecycleOwner.createViewBinding(dbClass, inflater, container, false)
        return _bind?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(savedInstanceState)

        initData(savedInstanceState)
    }

    open fun initView(savedInstanceState: Bundle?){}

    open fun initData(savedInstanceState: Bundle?){}
}