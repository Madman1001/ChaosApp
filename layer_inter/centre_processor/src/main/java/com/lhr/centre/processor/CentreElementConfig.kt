package com.lhr.centre.processor

/**
 * @author lhr
 * @date 2021/9/1
 * @des 模块配置信息类
 */
internal class CentreElementConfig(val elementName: String) {
    val extra = ArrayList<CentreElementPluginConfig>()
}

/**
 * @author lhr
 * @date 2021/9/1
 * @des 模块插件配置信息类
 */
internal class CentreElementPluginConfig(val name: String, val value: String){
    /**
     * 插件类型
     */
    enum class PluginType {
        Activity, Service, Broadcast, ContentProvider
    }
}