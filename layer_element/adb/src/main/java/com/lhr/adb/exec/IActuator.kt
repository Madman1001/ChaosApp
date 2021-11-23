package com.lhr.adb.exec

/**
 * @author lhr
 * @date 2021/11/20
 * @des 执行器接口
 */
interface IActuator {

    /**
     * 添加命令
     */
    fun addCommand(command: String)

    /**
     * 移除命令
     */
    fun removeCommand(command: String)

    /**
     * 执行命令
     */
    fun execCommand()
}