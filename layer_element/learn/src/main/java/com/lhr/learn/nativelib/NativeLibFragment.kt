package com.lhr.learn.nativelib

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
import com.lhr.common.ui.BaseAdapter
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.R
import com.lhr.learn.databinding.FragmentNativeLibCheckBinding
import com.lhr.learn.procfile.FilesystemsFragment
import com.lhr.learn.utils.NativeLibScanUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * @CreateDate: 2022/8/16
 * @Author: mac
 * @Description: native库查看工具
 */
class NativeLibFragment : BaseFragment<FragmentNativeLibCheckBinding>() {
    private var inputManager: InputMethodManager? = null

    private var allNativeLibList = listOf<File>()

    private var selectClassList = listOf<File>()

    private val classDataAdapter = object : BaseAdapter<File>() {
        override fun bind(holder: ViewHolder, position: Int, data: File) {
            holder.itemView.findViewById<TextView>(R.id.itemTv).text = data.absolutePath
            holder.itemView.setOnClickListener {
                gotoNativeLibDetail(data.absolutePath)
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
            allNativeLibList = NativeLibScanUtil.getAllNativeLibraryFile(requireContext())
            classDataAdapter.replaceData(allNativeLibList)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initInputEditView() {
        mBinding.adbInCode.run {
            doOnTextChanged { text, start, before, count ->
                Log.e("TAG", "onTextChanged $text $start $before $count")
                if (TextUtils.isEmpty(text)) {
                    classDataAdapter.replaceData(allNativeLibList)
                } else {
                    selectClassList = allNativeLibList.filter {
                        it.absolutePath.contains(text ?: ".*", true)
                    }
                    classDataAdapter.replaceData(selectClassList)
                }

            }
            val root = mBinding.root
            if (root is ViewGroup) {
                root.run {
                    val focusView = View(context)
                    val param = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    addView(focusView, param)
                    focusView.setOnTouchListener(View.OnTouchListener { v, event ->
                        if (mBinding.adbInCode.isFocused) {
                            val position = IntArray(2) { 0 }
                            mBinding.adbInCode.getLocationInWindow(position)
                            if (event.rawY.toInt() !in position[1]..position[1] + mBinding.adbInCode.height) {
                                hideSoftInput(focusView)
                                return@OnTouchListener true
                            }
                        }
                        return@OnTouchListener false
                    })
                }
            }

        }
    }

    private fun hideSoftInput(view: View) {
        view.run {
            val manager = inputManager ?: context.getSystemService()
            if (inputManager == null) {
                inputManager = manager
            }
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            inputManager?.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    fun checkClass() {
        val text = mBinding.adbInCode.text.toString()
        if (text.isEmpty()) {
            return
        }

        gotoNativeLibDetail(text)
    }

    fun clearText() {
        mBinding.adbInCode.setText("")
    }

    fun gotoNativeLibDetail(path: String) {
        FilesystemsFragment.start(attachActivity, path)
    }
}