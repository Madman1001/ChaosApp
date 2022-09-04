package com.lhr.adb

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhr.adb.adapter.ResultAdapter
import com.lhr.adb.adbshell.AdbTerminal
import com.lhr.adb.databinding.FragmentAdbClientBinding
import com.lhr.common.ext.initInputBar
import com.lhr.common.ui.BaseFragment
import com.lhr.common.ui.startFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author lhr
 * @date 4/9/2022
 * @des adb client fragment
 */
class AdbClientFragment : BaseFragment<FragmentAdbClientBinding>() {
    private val TAG = this::class.java.simpleName

    private var index: Int = 0

    private val adapter = ResultAdapter()

    private var codeEditText: String
        get() = mBinding.inputEditText.text.toString()
        set(value) = mBinding.inputEditText.setText(value)

    private var ipAddress = ""

    private var port = -1

    private var adbTerminal: AdbTerminal? = null

    private lateinit var viewModel: ConnectViewModel

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        viewModel = ViewModelProviders.of(attachActivity).get(ConnectViewModel::class.java)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.connectList.size == 1){
                        viewModel.connectList.removeAt(index)
                        viewModel.notificationList.value = (viewModel.notificationList.value ?: 0) + 1
                    } else {
                        attachActivity.finish()
                    }
                }
            })
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        ipAddress = arguments?.getString(KEY_ADB_ADDRESS) ?: ""
        port = arguments?.getInt(KEY_ADB_PORT) ?: 0
        index = arguments?.getInt(KEY_FRAGMENT_INDEX) ?: 0
        mBinding.pager = this

        mBinding.inputEditText.run {
            doOnTextChanged { text, start, before, count ->
                if (text?.endsWith("\n") == true) {
                    runCode()
                }
            }
            initInputBar(mBinding.root as ViewGroup)
        }

        mBinding.resultRv.run {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = this@AdbClientFragment.adapter
        }

        connectADBD(ipAddress, port)
    }

    fun runCode() {
        val commands = codeEditText
        if (commands.isEmpty()) {
            return
        }
        codeEditText = ""
        adbTerminal?.runCommand(commands)
    }

    fun cleanResult() {
        adapter.replaceData(emptyList())
    }

    fun connectADBD(address: String, port: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                adbTerminal = AdbTerminal(attachActivity.application, address, port)
                adbTerminal?.start {
                    lifecycleScope.launch {
                        adapter.addMessage(it)
                    }
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, it.message + "")
                    Toast.makeText(attachActivity, it.message + "", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    companion object {
        private const val KEY_ADB_ADDRESS = "KEY_ADB_ADDRESS"
        private const val KEY_ADB_PORT = "KEY_ADB_PORT"
        private const val KEY_FRAGMENT_INDEX = "KEY_FRAGMENT_INDEX"

        fun create(index: Int, address: String, port: Int): Fragment {
            val fragment = AdbClientFragment()
            val bundle = Bundle().apply {
                putString(KEY_ADB_ADDRESS, address)
                putInt(KEY_ADB_PORT, port)
                putInt(KEY_FRAGMENT_INDEX, index)
            }
            fragment.arguments = bundle
            return fragment
        }

        fun start(activity: Activity, address: String, port: Int) {
            activity.startFragment<AdbClientFragment>(Bundle().apply {
                putString(KEY_ADB_ADDRESS, address)
                putInt(KEY_ADB_PORT, port)
            })
        }
    }

}