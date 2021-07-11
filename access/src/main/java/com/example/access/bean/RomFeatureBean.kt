package com.example.access.bean

/**
 * @author lhr
 * @date 2021/7/9
 * @des rom 系统特征类
 */
data class RomFeatureBean(val code: Int){
    var desc = ""
    val features = ArrayList<FeatureBean>()
    data class FeatureBean(val key: String, val value: String, val condition: String)

    override fun toString(): String {
        return "RomFeatureBean(code=$code, desc='$desc')"
    }
}
