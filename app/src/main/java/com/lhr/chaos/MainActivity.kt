package com.lhr.chaos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lhr.centre.Centre
import com.lhr.chaos.adapter.ButtonAdapter
import com.lhr.common.ext.live

/**
 * @author lhr
 * @date 2021/4/27
 * @des 应用主Activity
 */
class MainActivity : AppCompatActivity() {
    private val buttonAdapter = ButtonAdapter(ArrayList<Button>())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = this.findViewById<RecyclerView>(R.id.main_recycler_view)
        val layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = buttonAdapter
        if (Centre.isInit) {
            initListButton()
        } else {
            Centre.setInitCallback({ initListButton() }.live(this.lifecycle))
        }
    }

    private fun initListButton() {
        for (element in Centre.getElementList()) {
            for (entry in element.extraMap) {
                val bt = Button(this)
                bt.text = entry.key
                bt.setOnClickListener {
                    val intent = Intent(this, Class.forName(entry.value) as Class<*>)
                    this.startActivity(intent)
                }
                buttonAdapter.addButton(bt)
            }
        }
        buttonAdapter.notifyDataSetChanged()
    }
}