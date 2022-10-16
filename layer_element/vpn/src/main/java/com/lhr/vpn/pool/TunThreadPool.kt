package com.lhr.vpn.pool

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author lhr
 * @date 15/10/2022
 * @des 线程池
 */
val RunPool by lazy { TunThreadPool.mThreadPool }
object TunThreadPool {
    private val DEFAULT_IO_MAIN_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2
    private const val DEFAULT_KEEP_ALIVE_TIME = 6L
    val mThreadPool by lazy {
        ThreadPoolExecutor(
            DEFAULT_IO_MAIN_POOL_SIZE,
            DEFAULT_IO_MAIN_POOL_SIZE,
            DEFAULT_KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            ThreadPoolExecutor.CallerRunsPolicy()
        )
    }
}