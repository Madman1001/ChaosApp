package com.lhr.common.ui.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ListLiveData<T> : ArrayList<T>() {

    private val _listStatusChangeNotificator = MutableLiveData<ListLiveDataChangeEvent>()

    val changeNotificator: LiveData<ListLiveDataChangeEvent> = _listStatusChangeNotificator

    override fun add(element: T): Boolean {
        super.add(element)
        _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                listLiveDataState = ListLiveDataState.ITEM_RANGE_INSERTED,
                startIndex = size - 1,
                itemCount = 1
        )
        return true
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                listLiveDataState = ListLiveDataState.ITEM_RANGE_INSERTED,
                startIndex = index,
                itemCount = 1
        )
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val oldSize = size
        val added = super.addAll(elements)
        if (added) {
            _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                    listLiveDataState = ListLiveDataState.ITEM_RANGE_INSERTED,
                    startIndex = oldSize,
                    itemCount = size - oldSize
            )
        }
        return added
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val added = super.addAll(index, elements)
        if (added) {
            _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                    listLiveDataState = ListLiveDataState.ITEM_RANGE_INSERTED,
                    startIndex = index,
                    itemCount = elements.size
            )
        }
        return added
    }

    override fun clear() {
        val oldSize = size
        super.clear()
        if (oldSize != 0) {
            _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                    listLiveDataState = ListLiveDataState.ITEM_RANGE_REMOVED,
                    startIndex = 0,
                    itemCount = oldSize
            )
        }
    }

    override fun removeAt(index: Int): T {
        val value = super.removeAt(index)
        _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                listLiveDataState = ListLiveDataState.ITEM_RANGE_REMOVED,
                startIndex = index,
                itemCount = 1
        )
        return value
    }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        return if (index >= 0) {
            removeAt(index)
            true
        } else {
            false
        }
    }

    override fun set(index: Int, element: T): T {
        val value = super.set(index, element)
        _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                listLiveDataState = ListLiveDataState.ITEM_RANGE_CHANGED,
                startIndex = index,
                itemCount = 1
        )
        return value
    }

    fun replace(elements: Collection<T>) {
        super.clear()
        super.addAll(elements)
        _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                listLiveDataState = ListLiveDataState.CHANGED
        )
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        _listStatusChangeNotificator.value = ListLiveDataChangeEvent(
                listLiveDataState = ListLiveDataState.ITEM_RANGE_REMOVED,
                startIndex = fromIndex,
                itemCount = toIndex - fromIndex
        )
    }
}