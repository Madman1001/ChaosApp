package com.lhr.common.ui

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * @CreateDate: 2022/8/26
 * @Author: mac
 * @Description:
 */
class BaseFragmentAdapter<T> : FragmentStateAdapter{
    private val mListData = ArrayList<T>()

    private var ids = mListData.map { it.hashCode().toLong() }.toMutableList()

    private val createdIds = hashSetOf<Long>()

    private var creator: (Int, T)-> Fragment

    constructor(fragment: Fragment, creator: (Int, T)-> Fragment) : super(fragment){
        this.creator = creator
    }

    constructor(fragmentActivity: FragmentActivity, creator: (Int, T)-> Fragment) : super(fragmentActivity){
        this.creator = creator
    }

    constructor(fragmentManager: FragmentManager, lifecycle: Lifecycle, creator: (Int, T)-> Fragment) : super(
        fragmentManager,
        lifecycle
    ){
        this.creator = creator
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.itemAnimator = null
    }

    override fun getItemId(position: Int): Long {
        return ids[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return createdIds.contains(itemId)
    }

    override fun getItemCount(): Int = mListData.size

    override fun createFragment(position: Int): Fragment {
        val id = ids[position]
        createdIds.add(id)
        val item = mListData[position]
        return creator.invoke(position, item)
    }

    fun addData(data: List<T>) {
        val startIndex = itemCount
        mListData.addAll(data)
        ids.addAll(data.map { it.hashCode().toLong() })
        notifyItemRangeChanged(startIndex, data.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun replaceData(data: List<T>) {
        mListData.clear()
        ids.clear()
        addData(data)
        notifyDataSetChanged()
    }
}