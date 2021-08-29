package com.example.access.bean

import java.util.ArrayList

/**
 * @author lhr
 * @date 2021/7/9
 * @des 任务数据类
 */
data class TaskBean(val type: Int,val name: String,val priority:Int = 0) {
    val actionList: ArrayList<ActionBean> = ArrayList()

    override fun toString(): String {
        return "TaskBean(type=$type, name='$name', priority=$priority, actionList=$actionList)"
    }
}