package com.lhr.centre.processor

import javax.lang.model.element.Element

/**
 * @author lhr
 * @date 2021/9/1
 * @des 模块配置信息类
 */
internal class CentreElementConfig(val elementName: String) {
    val extra = ArrayList<Element>()
}