package com.lhr.chaos.adapter

import android.widget.Button
import com.lhr.chaos.R
import com.lhr.common.ui.BaseAdapter

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
class ButtonAdapter : BaseAdapter<ButtonAdapter.ListData>() {

    override fun bind(holder: ViewHolder, position: Int, data: ListData) {
        holder.itemView.findViewById<Button>(R.id.itemBtn).run {
            text = data.name
            setOnClickListener {
                data.action.invoke()
            }
        }
    }

    override var layout: Int = R.layout.item_module_list

    data class ListData(val name: String, val action: () -> Unit)
}