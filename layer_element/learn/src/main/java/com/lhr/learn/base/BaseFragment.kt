package com.lhr.learn.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
abstract class BaseFragment: Fragment() {
    abstract fun getLayout(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = getLayout()
        if (layoutId != 0) {
            return inflater.inflate(layoutId, container, false)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}