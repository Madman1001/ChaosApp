package com.example.myapplication.adapter

import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
class ButtonAdapter(private val list: ArrayList<Button>) :
    RecyclerView.Adapter<ButtonAdapter.ViewHandler>() {

    class ViewHandler(val buttonContainer: ViewGroup) : RecyclerView.ViewHolder(buttonContainer)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHandler {
        val view = FrameLayout(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(-1,-2)
        return ViewHandler(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHandler, position: Int) {
        holder.buttonContainer.removeAllViews()
        val param = FrameLayout.LayoutParams(-1,-2)
        param.gravity = Gravity.CENTER
        holder.buttonContainer.addView(list[position],param)
    }

    fun removeButton(button: Button): Boolean {
        return list.remove(button)
    }

    fun removeButton(position: Int): Button {
        return list.removeAt(position)
    }

    fun addButton(button: Button): Boolean {
        return list.add(button)
    }
}