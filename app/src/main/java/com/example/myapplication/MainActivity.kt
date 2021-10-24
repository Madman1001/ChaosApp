package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.ButtonAdapter
import com.example.sys.utils.ApplicationUtil
import com.example.utils.ActivityLaunchUtils
import com.example.utils.live
import com.lhr.centre.Centre

class MainActivity : AppCompatActivity() {
    private val buttonAdapter = ButtonAdapter(ArrayList<Button>())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setTheme(R.style.Theme_MyApplication_MainActivity)
        setContentView(R.layout.activity_main)
        val recyclerView = this.findViewById<RecyclerView>(R.id.main_recycler_view)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = buttonAdapter
        if (Centre.isInit) {
            initListButton()
        } else {
            Centre.setInitCallback({ initListButton() }.live(this.lifecycle))
        }

        this.startService(Intent(this, MainService::class.java))

        ApplicationUtil.getRunningServices()
    }

    private fun initListButton() {
        for (element in Centre.getElementList()) {
            for (entry in element.extraMap) {
                val bt = Button(this)
                bt.text = entry.key
                bt.setOnClickListener {
                    val intent = Intent(this, Class.forName(entry.value) as Class<*>)
                    ActivityLaunchUtils.launchActivity(this, intent)
                }
                buttonAdapter.addButton(bt)
            }
        }
        buttonAdapter.notifyDataSetChanged()
    }
}