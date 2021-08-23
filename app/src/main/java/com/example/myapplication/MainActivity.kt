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
import com.example.utils.UtilActivity
import com.example.game.GameActivity
import com.example.utils.start
import com.lhr.view.ViewActivity

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
        val gotoUtil = Button(this)
        val gotoGame = Button(this)

        gotoAdb.setOnClickListener {
            //跳转adb页面
            startActivity(Intent(this, AdbActivity::class.java))
        }

        gotoGame.setOnClickListener {
            //跳转game页面
            start(GameActivity::class.java)
        }

        gotoView.setOnClickListener {
            //跳转view页面
            start(ViewActivity::class.java)
        }

        gotoSys.setOnClickListener {
            //跳转sys页面
            start(SysActivity::class.java)
        }

        gotoAccess.setOnClickListener {
            //跳转无障碍页面
            start(AccessActivity::class.java)
        }

        gotoUtil.setOnClickListener {
            //跳转工具页面
            start(UtilActivity::class.java)
        }

        gotoAdb.text = "跳转adb页面"
        gotoGame.text = "跳转game页面"
        gotoView.text = "跳转view页面"
        gotoSys.text = "跳转sys页面"
        gotoAccess.text = "跳转无障碍页面"
        gotoUtil.text = "跳转工具页面"

        buttonAdapter.addButton(gotoAdb)
        buttonAdapter.addButton(gotoView)
        buttonAdapter.addButton(gotoGame)
        buttonAdapter.addButton(gotoSys)
        buttonAdapter.addButton(gotoAccess)
        buttonAdapter.addButton(gotoUtil)
    }

}