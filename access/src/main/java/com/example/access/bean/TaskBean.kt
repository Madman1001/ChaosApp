package com.example.access.bean

import java.util.ArrayList

/**
 * @author lhr
 * @date 2021/7/9
 * @des 权限规则类
 */
data class TaskBean(val type: Int,val name: String) {
    var priority:Int = 0
    var checkable:Boolean = true
    val actionList: ArrayList<ActionBean> = ArrayList()

    override fun toString(): String {
        return "PermissionRuleBean(type=$type, priority=$priority, checkable=$checkable, actionList=$actionList)"
    }
}