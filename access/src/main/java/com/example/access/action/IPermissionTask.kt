package com.example.access.action

import android.content.Context

/**
 * @author lhr
 * @date 2021/7/8
 * @des 权限动作接口
 */
interface IPermissionTask {
    /**
     * 申请权限
     */
    fun requestPermission(context: Context)

    /**
     * 检查权限
     */
    fun checkPermission(context: Context):Boolean
}