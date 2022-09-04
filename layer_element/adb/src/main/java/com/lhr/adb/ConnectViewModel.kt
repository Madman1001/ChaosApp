package com.lhr.adb

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author lhr
 * @date 4/9/2022
 * @des
 */
class ConnectViewModel: ViewModel(){
    val connectList: ArrayList<ConnectConfig> = arrayListOf()

    val notificationList: MutableLiveData<Int> = MutableLiveData(0)
}