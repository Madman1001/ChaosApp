package com.lhr.vpn.pool

/**
 * @author lhr
 * @date 15/10/2022
 * @des
 */
class TunRunnable(
    private val threadName: String,
    private val realRunnable: Runnable
) : Runnable {
    override fun run() {
        val preName = Thread.currentThread().name
        Thread.currentThread().name = threadName
        realRunnable.run()
        Thread.currentThread().name = preName
    }
}