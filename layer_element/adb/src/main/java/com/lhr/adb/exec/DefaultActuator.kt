package com.lhr.adb.exec

import com.lhr.common.ext.readText
import java.io.DataOutputStream
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

    private val linkedDeque: LinkedList<String> = LinkedList<String>()

    override fun addCommand(command: String) {
        linkedDeque.addLast(command)
    }


    override fun removeCommand(command: String) {
        linkedDeque.remove(command)
    }

    override fun execCommand() {
        var process: Process? = null
        var dos: DataOutputStream? = null
        try {
            for (command in linkedDeque) {
                if (process == null){
                    process = mRuntime.exec(command)
                    dos = DataOutputStream(process.outputStream)
                }else{
                    dos?.writeBytes("$command\n")
                }
            }
            dos?.flush()
            dos?.close()
            val success = process?.let {
                try {
                    it.inputStream.readText()
                }catch (e: Exception){
                    ""
                }
            } ?: ""


            val fail = process?.let {
                try {
                    it.errorStream.readText()
                }catch (e: Exception){
                    ""
                }
            } ?: ""
            if (success.isNotEmpty()){
                listener.invoke("", true, success)
            }else{
                listener.invoke("", false, fail)
            }
        } catch (e: Exception) {
            listener.invoke("", false, e.toString())
        }
    }

}