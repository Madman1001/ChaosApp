package com.lhr.sys.service

import android.os.IBinder

/**
 * @CreateDate: 2022/7/22
 * @Author: mac
 * @Description: binder元数据
 */
data class ServiceHookBean(
    val serviceName: String,
    val stub: Class<*>,
    val iinterface: Class<*>,
    val extAction: (IBinder)->Unit = {_ -> } //额外操作
)
