package com.lhr.adb

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lhr.adb.adapter.ResultAdapter
import com.lhr.adb.databinding.ActivityAdbBinding
import com.lhr.adb.exec.DefaultActuator
import com.lhr.adb.script.*
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import kotlinx.coroutines.*

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
@CElement(name = "命令行模块")
class AdbActivity : BaseActivity<ActivityAdbBinding>(){
    private val tag = "AS_${this::class.java.simpleName}"
    private val adapter = ResultAdapter()

    private var codeEditText: String
        get() = mBinding.adbInCode.text.toString()
        set(value) = mBinding.adbInCode.setText(value)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        this.actionBar?.show()
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = this.menuInflater
        menuInflater.inflate(R.menu.adb_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.adb_open_connect -> OpenAdbScript("5555")
            R.id.adb_show_ip -> ShowIpScript()
            R.id.adb_show_packages -> ShowPackagesScript()
            R.id.adb_dump -> DumpScript()
            R.id.adb_test -> TestScript()
            else -> null
        }?.run {
            this.listener = { command, result, message ->
                Log.d(tag, "$command: $result : $message")
                GlobalScope.launch(Dispatchers.Main) {
                    adapter.addMessage(message)
                    mBinding.adbOutResult.scrollToPosition(adapter.itemCount - 1)
                }
            }
            this.start()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init(){
        mBinding.adbOutResult.run {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = this@AdbActivity.adapter
        }

        mBinding.adbInRun.setOnClickListener {
            runCode()
        }

        mBinding.adbOutClean.setOnClickListener {
            adapter.replaceData(emptyList())
        }
    }

    private fun runCode(){
        val codeCommand = codeEditText
        codeEditText = ""
        if (codeCommand.isEmpty()){
            return
        }
        val actuator = DefaultActuator { command, result, message ->
            Log.e(tag, message)
            GlobalScope.launch(Dispatchers.Main) {
                adapter.addMessage(message)
                mBinding.adbOutResult.scrollToPosition(adapter.itemCount - 1)
            }
        }
        actuator.addCommand(codeCommand)
        GlobalScope.launch {
            actuator.execCommand()
        }
    }
}