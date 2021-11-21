package com.lhr.adb.exce

import com.lhr.utils.IOUtils

/**
 * @author lhr
 * @date 2021/4/28
 * @des 脚本执行实体
 */
class CommandRunnable(
    val command: String,
    private val mRuntime: Runtime = Runtime.getRuntime(),
    private val resultCallback: (Boolean, String) -> Boolean
) : Runnable {

    /**
     * 执行命令
     */
    override fun run() {
        try {
            val result = mRuntime.exec(command)
            val success = IOUtils.readStream(result.inputStream)
            val fail = IOUtils.readStream(result.errorStream)
            if (success.isNotEmpty()) {
                result.destroy()
                resultCallback.invoke(true, success)
            } else {
                result.destroy()
                resultCallback.invoke(false, fail)
            }
        } catch (e: Exception) {
            resultCallback.invoke(false, e.toString())
        }
    }
}