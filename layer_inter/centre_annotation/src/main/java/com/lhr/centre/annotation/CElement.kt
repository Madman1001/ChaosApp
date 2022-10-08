package com.lhr.centre.annotation

/**
 * @author lhr
 * @date 2021/9/1
 * @des 组件注册注解
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
annotation class CElement(val name: String, val flag: Int = 0)

const val CELEMENT_FLAG_LAUNCHER = 0x01
