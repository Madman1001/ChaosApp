package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.ButtonAdapter
import com.example.utils.live
import com.example.utils.start
import com.lhr.centre.Centre

class MainActivity : AppCompatActivity() {
    private val buttonAdapter = ButtonAdapter(ArrayList<Button>())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = this.findViewById<RecyclerView>(R.id.main_recycler_view)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = buttonAdapter
        if (Centre.isInit){
            initListButton()
        }else{
            Centre.setInitCallback({initListButton()}.live(this.lifecycle))
        }
    }

    private fun initListButton() {
        for (element in Centre.getElementList()) {
            for (entry in element.extraMap) {
                val bt = Button(this)
                bt.text = entry.key
                bt.setOnClickListener {
                    this.start(Class.forName(entry.value) as Class<out Activity>)
                }
                buttonAdapter.addButton(bt)
            }
        }
        buttonAdapter.notifyDataSetChanged()
    }

}