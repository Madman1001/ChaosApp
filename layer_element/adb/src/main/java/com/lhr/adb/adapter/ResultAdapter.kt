package com.lhr.adb.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lhr.common.ui.BaseAdapter

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
class ResultAdapter : BaseAdapter<String>() {
    override fun bind(holder: ViewHolder, position: Int, data: String) {
        (holder.itemView as TextView).text = "Result: $data"
    }

    override fun createView(parent: ViewGroup, viewType: Int): View {
        return TextView(parent.context).apply {
            isAllCaps = false
            isActivated = false
            textSize = 15f
            setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    fun addMessage(vararg item: String){
        addData(item.toList())
    }

    override var layout: Int = 0
}