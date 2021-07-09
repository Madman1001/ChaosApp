package com.example.access.bean

/**
 * @author lhr
 * @date 2021/7/9
 * @des rom 系统规则类
 */
data class RomRuleBean(val version: Int) {
    val permissionRuleBeans:ArrayList<PermissionRuleBean> = ArrayList()

    override fun toString(): String {
        return "RomRuleBean(version=$version, permissionRuleBeans=$permissionRuleBeans)"
    }
}