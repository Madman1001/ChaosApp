package com.example.access.bean

/**
 * @author lhr
 * @date 2021/7/9
 * @des 权限跳转类
 */
data class PermissionIntentBean(val id: Long = System.currentTimeMillis()) : Cloneable {
    var permissionAction: String = ""
    var permissionActivity: String = ""
    var permissionPackage: String = ""
    var permissionExtra: String = ""
    var permissionData: String = ""
    override fun toString(): String {
        return "PermissionIntentBean(id=$id, permissionAction='$permissionAction', permissionActivity='$permissionActivity', permissionPackage='$permissionPackage', permissionExtra='$permissionExtra', permissionData='$permissionData')"
    }

}