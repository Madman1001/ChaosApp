package com.lhr.chaos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
    private val buttonAdapter by lazy { ButtonAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.findViewById<RecyclerView>(R.id.main_recycler_view).run {
            layoutManager = GridLayoutManager(this.context, 2, GridLayoutManager.VERTICAL, false)
            adapter = buttonAdapter
        }
        if (Centre.isInit) {
            initListButton()
        } else {
            Centre.setInitCallback({ initListButton() }.live(this.lifecycle))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initListButton() {
        val resultList = mutableListOf<ButtonAdapter.ListData>()
        for (element in Centre.getElementList()) {
            for (entry in element.extraMap) {
                resultList.add(
                    ButtonAdapter.ListData(
                        entry.key
                    ) {
                        val intent = Intent(this, Class.forName(entry.value) as Class<*>)
                        this.startActivity(intent)
                    }
                )
            }
        }
        buttonAdapter.replaceData(resultList)
    }
}