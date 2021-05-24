package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adb.AdbActivity
import com.example.myapplication.adapter.ButtonAdapter
import com.example.view.GameActivity
import java.lang.RuntimeException

class MainActivity : AppCompatActivity(){
    private val buttonAdapter = ButtonAdapter(ArrayList<Button>())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListButton()
        val recyclerView = this.findViewById<RecyclerView>(R.id.main_recycler_view)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = buttonAdapter
    }

    private fun initListButton(){
        val gotoAdb = Button(this)
        val gotoView = Button(this)

        gotoAdb.setOnClickListener {
            //跳转adb页面
            startActivity(Intent(this,AdbActivity::class.java))
        }

        gotoView.setOnClickListener {
            //跳转view页面
            startActivity(Intent(this,GameActivity::class.java))
        }

        gotoAdb.text = "跳转adb页面"
        gotoView.text = "跳转view页面"

        buttonAdapter.addButton(gotoAdb)
        buttonAdapter.addButton(gotoView)
    }
}