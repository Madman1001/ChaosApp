package com.lhr.adb

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lhr.adb.adapter.ResultAdapter
import com.lhr.adb.exec.DefaultActuator
import com.lhr.adb.script.*
import com.lhr.centre.annotation.CElement
import kotlinx.coroutines.*
import java.util.*

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
@CElement(name = "ADB功能")
class AdbActivity : AppCompatActivity(){
    private val tag = "AS_${this::class.java.simpleName}"
    private val adapter = ResultAdapter(ArrayList<String>())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adb)
        val ab = this.actionBar
        ab?.show()
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
                    adapter.addItem(message)
                    findViewById<RecyclerView>(R.id.adb_out_result).scrollToPosition(adapter.itemCount - 1)
                }
            }
            this.start()
        }
        return super.onOptionsItemSelected(item)
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
        val codeET = findViewById<EditText>(R.id.adb_in_code)
        val codeCommand = codeET.text.toString()
        codeET.setText("")
        if (codeCommand.isEmpty()){
            return
        }
        val actuator = DefaultActuator { command, result, message ->
            Log.e(tag, message)
            GlobalScope.launch(Dispatchers.Main) {
                adapter.addItem(message)
                findViewById<RecyclerView>(R.id.adb_out_result).scrollToPosition(adapter.itemCount - 1)
            }
        }
        actuator.addCommand(codeCommand)
        GlobalScope.launch {
            actuator.execCommand()
        }
    }
}