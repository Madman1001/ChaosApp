package com.lhr.adb.exce

import com.lhr.utils.IOUtils
import java.io.DataOutputStream

/**
 * @author lhr
 * @date 2021/4/28
 * @des 脚本执行实体
 */
class CommandExecute(
    val command: String,
    private val resultCallback: (Boolean, String) -> Boolean
) : IExecute {

    /**
     * 执行命令
     */
    override fun exce(process: Process) {
        try {
            val output = DataOutputStream(process.outputStream)
            output.write("$command\n".toByteArray())
            val success =
                try {
                    IOUtils.readStream(process.inputStream)
                }catch (e: Exception){
                    ""
                }

            val fail =
                try {
                    IOUtils.readStream(process.errorStream)
                }catch (e: Exception){
                    ""
                }

            if (success.isNotEmpty() && success.isNotBlank()){
                resultCallback.invoke(true, success)
            }else{
                resultCallback.invoke(false, fail)
            }
        } catch (e: Exception) {
            resultCallback.invoke(false, e.toString())
        }
    }
}