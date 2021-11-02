package com.lhr.adb.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author lhr
 * @date 2021/4/28
 * @des
 */
object AdbRunner {
    private val mRuntime: Runtime = Runtime.getRuntime()
    private val mStringBuilder: StringBuilder = StringBuilder()

    suspend fun runCommand(command: String): String {
        return withContext(Dispatchers.IO){
            try {
                val result = mRuntime.exec(command)
                val success = readStream(result.inputStream)
                val fail = readStream(result.errorStream)

                if (!success.isNullOrEmpty()){
                    result.destroy()
                    success
                }else{
                    result.destroy()
                    fail
                }
            }catch (e:Exception){
                e.toString()
            }
        }
    }

    private fun readStream(input:InputStream) : String{
        mStringBuilder.clear()
        val reader = BufferedReader(InputStreamReader(input))
        val buffer = CharArray(512)
        var len = reader.read(buffer)
        while (len != -1){
            mStringBuilder.append(buffer,0,len)
            len = reader.read(buffer)
        }
        return mStringBuilder.toString()
    }
}