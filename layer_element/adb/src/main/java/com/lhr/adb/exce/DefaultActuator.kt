package com.lhr.adb.exce

import java.util.*

/**
 * @author lhr
 * @date 2021/11/20
 * @des 默认脚本执行器
 */
class DefaultActuator(
    var listener: (String, Boolean, String) -> Unit = { _, _, _ -> }
) : IActuator {
    private val tag = "DefaultActuator"

    private val mRuntime: Runtime = Runtime.getRuntime()

    private val linkedDeque: Deque<CommandRunnable> = LinkedList<CommandRunnable>()

    override fun addCommand(command: String) {
        CommandRunnable(command, mRuntime) { result, message ->
            listener.invoke(command, result, message)
            result
        }.apply {
            linkedDeque.addLast(this)
        }
    }


    override fun removeCommand(command: String) {
        for (runnable in linkedDeque) {
            if (runnable.command == command) {
                linkedDeque.remove(runnable)
                break
            }
        }
    }

    override fun exceCommand() {
        for (runnable in this.linkedDeque) {
            runnable.run()
        }
    }

}