package com.lhr.vpn.channel

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue

/**
 * @CreateDate: 2022/10/14
 * @Author: mac
 * @Description: 客户端基础模型
 */
abstract class OneChannel<T>: Runnable {
    private val queue: LinkedBlockingQueue<T> = LinkedBlockingQueue()

    private var job: Job? = null

    abstract fun handData(data: T)

    fun sendData(data: T){
        queue.add(data)
    }

    fun startChannel(){
        if (job == null){
            job = GlobalScope.launch {
                this@OneChannel.run()
            }
        }
    }

    fun closeChannel(){
        job?.cancel()
        job = null
    }

    override fun run() {
        while (true){
            val data = queue.take() ?: break

            kotlin.runCatching {
                handData(data)
            }
            continue
        }
        job = null
    }
}