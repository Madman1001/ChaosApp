package com.lhr.adb.adbshell

import android.content.Context
import com.cgutman.adblib.AdbStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

/**
 * @author lhr
 * @date 4/9/2022
 * @des adb pseudo terminal
 */
class AdbTerminal(context: Context, private val address: String, private val port: Int) {
    private var adbClient = AdbClient(context)

    private var commandBuffer: LinkedBlockingQueue<String> = LinkedBlockingQueue<String>()

    private var callback: (String)->Unit = {}

    private var readJob: Job? = null
    private var writeJob: Job? = null

    fun start(resultCallback: (String)->Unit) {
        val stream = adbClient.openConnect(address, port)
        callback = resultCallback
        readJob = GlobalScope.launch { readResult(stream) }
        writeJob = GlobalScope.launch { writeBuffer(stream) }
    }

    fun stop(){
        writeJob?.cancel()
        writeJob = null
        readJob?.cancel()
        readJob = null
    }

    fun runCommand(command: String){
        synchronized(commandBuffer){
            commandBuffer.add(command)
        }
    }

    fun release(){
        stop()
        adbClient.closeConnect()
    }

    private suspend fun writeBuffer(stream: AdbStream){
        while (!stream.isClosed){
            kotlin.runCatching {
                val command = commandBuffer.take()
                stream.write( command + "\n")
            }
        }
    }

    private suspend fun readResult(stream: AdbStream){
        while (!stream.isClosed){
            kotlin.runCatching {
                val result = String(stream.read())
                callback.invoke(result)
            }
        }
    }
}