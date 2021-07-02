package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.access.AccessActivity
import com.example.adb.AdbActivity
import com.example.myapplication.adapter.ButtonAdapter
import com.example.sys.SysActivity
import com.example.view.GameActivity


class MainActivity : AppCompatActivity() {
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

    private fun initListButton() {
        val gotoAdb = Button(this)
        val gotoView = Button(this)
        val gotoSys = Button(this)
        val gotoAccess = Button(this)

        gotoAdb.setOnClickListener {
            //跳转adb页面
            startActivity(Intent(this, AdbActivity::class.java))
        }

        gotoView.setOnClickListener {
            //跳转view页面
            startActivity(Intent(this, GameActivity::class.java))
        }

        gotoSys.setOnClickListener {
            //跳转sys页面
            startActivity(Intent(this, SysActivity::class.java))
        }

        gotoAccess.setOnClickListener {
            //跳转至无障碍服务设置
            startActivity(Intent(this, AccessActivity::class.java))
        }

        gotoAdb.text = "跳转adb页面"
        gotoView.text = "跳转view页面"
        gotoSys.text = "跳转sys页面"
        gotoAccess.text = "跳转无障碍页面"

        buttonAdapter.addButton(gotoAdb)
        buttonAdapter.addButton(gotoView)
        buttonAdapter.addButton(gotoSys)
        buttonAdapter.addButton(gotoAccess)
    }

}