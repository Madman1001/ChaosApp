package com.lhr.common.ext

import android.view.View

/**
 * @CreateDate: 2022/4/22
 * @Author: mac
 * @Description:View 扩展方法
 */
fun View.visible(bool: Boolean){
    this.visibility = if (bool) View.VISIBLE else View.GONE
}

fun View.visible(): Boolean{
    return this.visibility == View.VISIBLE
}