package com.lhr.vpn.channel

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue

/**
 * @CreateDate: 2022/10/14
 * @Author: mac
 * @Description:
 */
abstract class StreamChannel<O> {
    protected val queue: LinkedBlockingQueue<O> = LinkedBlockingQueue()

    private var inputJob: Job? = null

    private var outputJob: Job? = null

    @Volatile
    var isOpened = false
        private set

    fun openChannel(){
        if (isOpened) return

        isOpened = true

        outputJob = startOutputJob()

        inputJob = startInputJob()
    }

    fun closeChannel(){
        if (!isOpened) return

        isOpened = false

        inputJob?.cancel()
        inputJob = null

        outputJob?.cancel()
        outputJob = null
    }

    fun sendData(data: O){
        queue.add(data)
    }

    abstract fun writeData(o: O)

    abstract fun readData()

    /**
     * 启动读取线程
     */
    private fun startInputJob(): Job {
        return GlobalScope.launch {
            while (true){
                kotlin.runCatching {
                    readData()
                }
            }
        }
    }

    /**
     * 启动写入线程
     */
    private fun startOutputJob(): Job {
        return GlobalScope.launch {
            while (true){
                kotlin.runCatching {
                    val data = queue.take()
                    writeData(data)
                }
            }
        }
    }
}