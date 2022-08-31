package com.lhr.adb.exec

import com.lhr.adb.IActuator
import java.util.*

/**
 * @author lhr
 * @date 2021/11/20
 * @des 同步执行器
 */
class SyncActuator(
    var listener: (String, Boolean, String) -> Unit = { _, _, _ -> }
) : IActuator {
    private val tag = "DefaultActuator"

    private val mRuntime: Runtime = Runtime.getRuntime()

    private val linkedDeque: LinkedList<CommandExecute> = LinkedList<CommandExecute>()

    override fun addCommand(command: String) {
        linkedDeque.addLast(CommandExecute(command){
            result,message ->
            listener.invoke(command,result,message)
            result
        })
    }


    override fun removeCommand(command: String) {
        linkedDeque.remove(linkedDeque.findLast { it.command == command })
    }

    override fun execCommand() {
        val process = mRuntime.exec("date")
        for(execute in linkedDeque){
            execute.exec(process)
        }
    }

}