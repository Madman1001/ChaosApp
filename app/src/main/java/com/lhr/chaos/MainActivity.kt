package com.lhr.chaos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lhr.centre.Centre
import com.lhr.centre.annotation.CELEMENT_FLAG_LAUNCHER
import com.lhr.centre.annotation.CElement
import com.lhr.centre.element.TableConstant
import com.lhr.chaos.adapter.ButtonAdapter
import com.lhr.common.ext.live

/**
 * @author lhr
 * @date 2021/4/27
 * @des 应用主Activity
 */
@CElement(name = "首页", CELEMENT_FLAG_LAUNCHER)
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
            val name = element.extraMap[TableConstant.EXTRA_NAME] ?: ""
            val clazzName = element.extraMap[TableConstant.EXTRA_VALUE] ?: ""

            if (clazzName == this::class.java.name) continue

            resultList.add(
                ButtonAdapter.ListData(name) {
                    val intent = Intent(this, Class.forName(clazzName) as Class<*>)
                    this.startActivity(intent)
                }
            )
        }
        buttonAdapter.replaceData(resultList)
    }
}