package com.lhr.vpn.socks.channel

import android.util.Log
import com.lhr.vpn.socks.Tun2Tap
import kotlinx.coroutines.*

/**
 * @CreateDate: 2022/10/14
 * @Author: mac
 * @Description: tun tap channel
 */
class StreamChannel(private val tun2Tap: Tun2Tap):BaseTunChannel<ByteArray, ByteArray>() {
    private val tag = this::class.java.simpleName + ":" + this.toString()

    private var inputJob: Job? = null

    private var outputJob: Job? = null

    val isInputChannelOpened get() = inputJob?.isActive == true

    val isOutputChannelOpened get() = outputJob?.isActive == true

    fun openChannel(scope: CoroutineScope = GlobalScope){
        if (!isInputChannelOpened){
            openInputChannel(scope)
        }

        if (!isOutputChannelOpened){
            openOutputChannel(scope)
        }
    }

    fun openInputChannel(scope: CoroutineScope = GlobalScope){
        inputJob?.cancel()
        inputJob = null

        inputJob = scope.launch(Dispatchers.IO) {
            while (isActive){
                kotlin.runCatching {
                    val data = tun2Tap.readTun()
                    if (data.isNotEmpty()){
                        receivePool.addLast(data)
                    }
                }
            }
        }
    }

    fun openOutputChannel(scope: CoroutineScope = GlobalScope){
        outputJob?.cancel()
        outputJob = null

        outputJob = scope.launch(Dispatchers.IO){
            while (isActive){
                val sendData = sendPool.takeFirst()
                if (sendData === CloseSign){
                    //empty data , close output
                    Log.e(tag, "shutdown tun2Tap output")
                    tun2Tap.close()
                    break
                }
                kotlin.runCatching {
                    tun2Tap.writeTun(sendData)
                }
            }
        }
    }

    fun closeChannel(){
        closeInputChannel()
        closeOutputChannel()
    }

    fun closeInputChannel(){
        inputJob?.cancel()
        inputJob = null
    }

    fun closeOutputChannel(){
        outputJob?.cancel()
        outputJob = null
    }
}