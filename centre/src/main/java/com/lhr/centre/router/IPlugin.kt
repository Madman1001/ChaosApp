package com.lhr.centre.router

/**
 * @author : lhr
 * @description :注册表接口
 * @date : 2021/8/15 16:48
 */
interface IPlugin {
    fun getTitle(): String

    fun getLevel(): Int

    fun getExtra(): String
}