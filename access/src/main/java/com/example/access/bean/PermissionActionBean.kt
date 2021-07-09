package com.example.access.bean

/**
 * @author lhr
 * @date 2021/7/9
 * @des 权限动作类
 */
data class PermissionActionBean(val id: Long = System.currentTimeMillis()) {
    var needWaitWindow: Boolean = false
    var needWaitTime: Int = 0
    var findTexts: ArrayList<String> = ArrayList()
    var scrollNode: String = ""
    var clickNode: String = ""
    var checkNode: CheckNode = CheckNode()
    var behavior: String = ""
    var notNeedBack: Boolean = false

    class CheckNode {
        var nodeClassName: String = ""
        var nodeStatus = false
    }

    override fun toString(): String {
        return "PermissionActionBean(id=$id, needWaitWindow=$needWaitWindow, needWaitTime=$needWaitTime, findTexts=$findTexts, scrollNode='$scrollNode', clickNode='$clickNode', checkNode=$checkNode, behavior='$behavior', notNeedBack=$notNeedBack)"
    }


}