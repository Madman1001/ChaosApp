package com.example.access.bean

/**
 * @author lhr
 * @date 2021/7/9
 * @des 权限动作类
 */
data class PermissionActionBean(val id: Long = System.currentTimeMillis()) : Cloneable{
    var needWaitWindow: Boolean = false
    var needWaitTime: Int = 0
    var findTexts: ArrayList<String> = ArrayList()
    var scrollNode: String = ""
    var clickNode: String = ""
    var checkNode: String = ""
    var checkStatus = true
    var behavior: String = ""
    var notNeedBack: Boolean = false

    override fun toString(): String {
        return "PermissionActionBean(id=$id, needWaitWindow=$needWaitWindow, needWaitTime=$needWaitTime, findTexts=$findTexts, scrollNode='$scrollNode', clickNode='$clickNode', checkNode='$checkNode', checkStatus=$checkStatus, behavior='$behavior', notNeedBack=$notNeedBack)"
    }

}