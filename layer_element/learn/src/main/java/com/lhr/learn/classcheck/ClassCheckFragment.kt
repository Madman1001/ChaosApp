package com.lhr.learn.classcheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhr.common.ext.initInputBar
import com.lhr.common.ui.BaseAdapter
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.R
import com.lhr.learn.databinding.FragmentClassCheckBinding
import com.lhr.learn.utils.ClassScanUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @CreateDate: 2022/8/16
 * @Author: mac
 * @Description: 类查看工具
 */
class ClassCheckFragment : BaseFragment<FragmentClassCheckBinding>() {
    private var allClassList = listOf<String>()

    private var selectClassList = listOf<String>()

    private val classDataAdapter = object : BaseAdapter<String>() {
        override fun bind(holder: ViewHolder, position: Int, data: String) {
            holder.itemView.findViewById<TextView>(R.id.itemTv).text = data
            holder.itemView.setOnClickListener {
                gotoClassDetail(data)
            }
        }

        override var layout: Int = R.layout.item_class_check_list
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.pager = this
        initInputEditView()

        initClassRecyclerView()
    }

    private fun initClassRecyclerView() {
        mBinding.adbOutResult.run {
            adapter = classDataAdapter
            layoutManager = LinearLayoutManager(context).apply {
                this.orientation = LinearLayoutManager.VERTICAL
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            allClassList = ClassScanUtil.getAllClass(requireContext())
            classDataAdapter.replaceData(allClassList)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initInputEditView() {
        mBinding.adbInCode.run {
            doOnTextChanged { text, start, before, count ->
                Log.e("TAG", "onTextChanged $text $start $before $count")
                if (TextUtils.isEmpty(text)) {
                    classDataAdapter.replaceData(allClassList)
                } else {
                    selectClassList = allClassList.filter {
                        it.contains(text ?: ".*", true)
                    }
                    classDataAdapter.replaceData(selectClassList)
                }
            }
            initInputBar(mBinding.root as ViewGroup)
        }
    }

    fun checkClass() {
        val text = mBinding.adbInCode.text.toString()
        if (text.isEmpty()) {
            return
        }

        gotoClassDetail(text)
    }

    fun clearText() {
        mBinding.adbInCode.setText("")
    }

    fun gotoClassDetail(className: String) {
        ClassDetailFragment.start(requireActivity(), className)
    }
}