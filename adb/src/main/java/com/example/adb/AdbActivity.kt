package com.example.adb

import android.app.Activity
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adb.adapter.ResultAdapter
import com.example.adb.utils.AdbRunner
import kotlinx.coroutines.*
import java.util.*

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
class AdbActivity : Activity(){
    private val results = ArrayList<String>()
    private val adapter = ResultAdapter(results)
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
            results.clear()
            adapter.notifyDataSetChanged()
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
            results.add(AdbRunner.runCommand(code))
            withContext(Dispatchers.Main){
                adapter.notifyDataSetChanged()
                findViewById<RecyclerView>(R.id.adb_out_result).scrollToPosition(adapter.itemCount-1)
            }
        }
    }
}