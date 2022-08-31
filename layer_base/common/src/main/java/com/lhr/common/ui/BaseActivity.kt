package com.lhr.common.ui

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.lhr.common.ext.createViewBinding
import com.lhr.common.ext.obtainBindingClassByParadigm

/**
 * @CreateDate: 2022/8/18
 * @Author: mac
 * @Description:
 */
abstract class BaseActivity<out DB: ViewDataBinding>: BaseNoDbActivity() {
    private var _bind: DB? = null

    val mBinding: DB get() = _bind!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbClass = obtainBindingClassByParadigm(this) as Class<DB>
        _bind = this.createViewBinding(dbClass, this.layoutInflater,null, false)
        _bind?.run {
            setContentView(this.root)
            initView(savedInstanceState)
        }
        initData(savedInstanceState)
    }

    open fun initView(savedInstanceState: Bundle?){}

    open fun initData(savedInstanceState: Bundle?){}
}