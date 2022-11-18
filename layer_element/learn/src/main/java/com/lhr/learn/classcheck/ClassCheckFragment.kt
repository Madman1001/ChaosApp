package com.lhr.learn.classcheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhr.common.ext.initInputBar
import com.lhr.common.ui.BaseAdapter
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.R
import com.lhr.learn.databinding.FragmentClassCheckBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @CreateDate: 2022/8/16
 * @Author: mac
 * @Description: 类查看工具
 */
class ClassCheckFragment : BaseFragment<FragmentClassCheckBinding>() {
    private lateinit var classesViewModel: ClassesViewModel

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
        classesViewModel = getClassesViewModel(attachActivity.application)

        mBinding.pager = this

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
            val list = classesViewModel.getClasses()
            withContext(Dispatchers.Main){
                classDataAdapter.replaceData(list)
                initInputEditView()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initInputEditView() {
        mBinding.adbInCode.run {
            val mAdapter = ArrayAdapter<String>(attachActivity, R.layout.item_text_view, classesViewModel.getClasses())
            this.setAdapter(mAdapter)
            doOnTextChanged { text, start, before, count ->
                Log.e("TAG", "onTextChanged $text $start $before $count")
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