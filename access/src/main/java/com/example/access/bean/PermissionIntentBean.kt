package com.example.access.bean

/**
 * @author lhr
 * @date 2021/7/9
 * @des 权限跳转类
 */
data class PermissionIntentBean(val action: String) {
    var permissionActivity:String = ""
    var permissionPackage:String = ""

    override fun toString(): String {
        return "PermissionIntentBean(action='$action', permissionActivity='$permissionActivity', permissionPackage='$permissionPackage')"
    }


}