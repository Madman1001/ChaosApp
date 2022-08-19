package com.lhr.common.ui.live

data class ListLiveDataChangeEvent(val listLiveDataState: ListLiveDataState,
                                   val startIndex: Int = -1,
                                   val itemCount: Int = -1)