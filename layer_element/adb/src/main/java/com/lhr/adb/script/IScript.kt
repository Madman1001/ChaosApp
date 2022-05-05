package com.lhr.adb.script

/**
 * @author lhr
 * @date 2021/11/21
 * @des 脚本接口
 */
interface IScript {

    var listener: (String, Boolean, String) -> Unit

    /**
     * 执行脚本
     */
    fun start()

    /**
     * 停止脚本
     */
    fun stop()
}