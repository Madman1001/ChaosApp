package com.lhr.common.ui

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
abstract class BaseAdapter<T>: RecyclerView.Adapter<BaseAdapter.ViewHolder>() {
    private val data: MutableList<T> = mutableListOf()
    private val mHandler = Handler(Looper.getMainLooper())

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(createView(parent, viewType))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder, position, data[position])
    }

    override fun getItemCount(): Int = data.size

    fun addData(list: Collection<T>){
        val preSize = data.size
        data.addAll(list)
        if (Looper.myLooper() == Looper.getMainLooper()){
            notifyItemRangeChanged(preSize, list.size)
        } else {
            mHandler.post {
                notifyItemRangeChanged(preSize, list.size)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun replaceData(list: Collection<T>){
        data.clear()
        data.addAll(list)
        if (Looper.myLooper() == Looper.getMainLooper()){
            notifyDataSetChanged()
        } else {
            mHandler.post {
                notifyDataSetChanged()
            }
        }
    }

    protected open fun createView(parent: ViewGroup, viewType: Int): View {
        return LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
    }

    abstract fun bind(holder: ViewHolder, position: Int, data: T)

    abstract var layout: Int
}