package com.example.adb

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adb.adapter.ResultAdapter
import com.example.adb.utils.AdbRunner
import com.lhr.centre.annotation.CElement
import kotlinx.coroutines.*
import java.util.*

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
@CElement(name = "ADB功能")
class AdbActivity : Activity(){
    private val tag = "AS_${this::class.java.simpleName}"
    private val adapter = ResultAdapter(ArrayList<String>())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adb)
        init()
    }

    private fun init(){
        val recyclerView = findViewById<RecyclerView>(R.id.adb_out_result)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        findViewById<View>(R.id.adb_in_run).setOnClickListener {
            runCode()
        }

        findViewById<View>(R.id.adb_out_clean).setOnClickListener {
            adapter.clearItem()
        }
    }

    private fun runCode(){
        GlobalScope.launch {
            val code = withContext(Dispatchers.Main){
                val codeET = findViewById<EditText>(R.id.adb_in_code)
                val codeCommand = codeET.text.toString()
                codeET.setText("")
                codeCommand
            }
            if (code.isEmpty()){
                return@launch
            }
            val result = AdbRunner.runCommand(code)
            Log.e(tag,result)
            withContext(Dispatchers.Main){
                adapter.addItem(result)
                findViewById<RecyclerView>(R.id.adb_out_result).scrollToPosition(adapter.itemCount-1)
            }
        }
    }
}