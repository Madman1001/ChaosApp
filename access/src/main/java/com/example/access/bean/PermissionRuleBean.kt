package com.example.access.bean

import java.util.ArrayList

/**
 * @author lhr
 * @date 2021/7/9
 * @des 权限规则类
 */
data class PermissionRuleBean(val type: Int) {
    var priority:Int = 0
    var checkable:Boolean = true
    var ruleIntent: PermissionIntentBean? = null
        get() {
            var intent: PermissionIntentBean? = null
            if (field != null){
                intent = field?.copy()
            }
            return intent
        }
    val actionList: ArrayList<PermissionActionBean> = ArrayList()

    override fun toString(): String {
        return "PermissionRuleBean(type=$type, priority=$priority, checkable=$checkable, ruleIntent=$ruleIntent, actionList=$actionList)"
    }

    fun getRuleActions():ArrayList<PermissionActionBean>{
        return actionList.clone() as ArrayList<PermissionActionBean>
    }
}