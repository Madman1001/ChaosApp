package com.lhr.utils

/**
 * @author lhr
 * @date 2021/3/26
 * @des 间隔限制工具
 */
class ClickMonitor(val interval: Long = 0) {
    /** 单位为ms */
    private var lastTime = 0L
    fun enable(): Boolean{
        if (System.currentTimeMillis() - lastTime > interval){
            lastTime = System.currentTimeMillis()
            return true
        }
        return false
    }
}