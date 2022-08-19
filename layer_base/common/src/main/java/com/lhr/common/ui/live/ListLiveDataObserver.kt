package com.lhr.common.ui.live

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

class ListLiveDataObserver(val adapter: RecyclerView.Adapter<*>)
    : Observer<ListLiveDataChangeEvent> {

    override fun onChanged(changeEvent: ListLiveDataChangeEvent?) {
        changeEvent?.let {
            when (it.listLiveDataState) {
                ListLiveDataState.CHANGED ->
                    adapter.notifyDataSetChanged()
                ListLiveDataState.ITEM_RANGE_CHANGED ->
                    adapter.notifyItemRangeChanged(it.startIndex, it.itemCount)
                ListLiveDataState.ITEM_RANGE_INSERTED ->
                    adapter.notifyItemRangeInserted(it.startIndex, it.itemCount)
                ListLiveDataState.ITEM_RANGE_REMOVED ->
                    adapter.notifyItemRangeRemoved(it.startIndex, it.itemCount)
            }
        }
    }
}