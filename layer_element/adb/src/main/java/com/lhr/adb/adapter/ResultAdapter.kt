package com.lhr.adb.adapter

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
class ResultAdapter(private val list:ArrayList<String>) : RecyclerView.Adapter<ResultAdapter.ResultHolder>(){
    class ResultHolder(var view: TextView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder {
        return ResultHolder(TextView(parent.context).apply {
            isAllCaps = false
            isActivated = false
        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ResultHolder, position: Int) {
        holder.view.textSize = 15f
        holder.view.setTextColor(Color.parseColor("#000000"))
        holder.view.text = "Result: ${list[position]}"
    }

    fun addItem(itemString: String){
        list.add(itemString)
        notifyDataSetChanged()
    }

    fun clearItem(){
        list.clear()
        notifyDataSetChanged()
    }
}