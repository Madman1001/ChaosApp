package com.lhr.vpn.channel

import com.lhr.vpn.pool.TunRunnable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingDeque

/**
 * @CreateDate: 2022/10/14
 * @Author: mac
 * @Description:
 */
abstract class StreamChannel<O> {
    protected val queue: LinkedBlockingDeque<O> = LinkedBlockingDeque()

    private var inputJob: Job? = null

    private var outputJob: Job? = null

    val isOpened get() = inputJob?.isActive == true && outputJob?.isActive == true

    fun openChannel(){
        if (isOpened) return

        outputJob = startOutputJob()

        inputJob = startInputJob()
    }

    fun closeChannel(){

        inputJob?.cancel()
        inputJob = null

        outputJob?.cancel()
        outputJob = null
    }

    fun sendData(data: O){
        queue.putLast(data)
    }

    abstract fun writeData(o: O)

    abstract fun readData()

    /**
     * 启动读取线程
     */
    private fun startInputJob(): Job {
        inputJob?.cancel()
        inputJob = null
        val inputRunnable = TunRunnable("$this-InputJob"){
            while (true){
                kotlin.runCatching {
                    readData()
                }
            }
        }
        return GlobalScope.launch(Dispatchers.IO){
            inputRunnable.run()
        }
    }

    /**
     * 启动写入线程
     */
    private fun startOutputJob(): Job {
        outputJob?.cancel()
        outputJob = null
        val outputRunnable = TunRunnable("$this-OutputJob"){
            while (true){
                val data = queue.takeFirst()
                writeData(data)
            }
        }
        return GlobalScope.launch(Dispatchers.IO){
            outputRunnable.run()
        }
    }
}