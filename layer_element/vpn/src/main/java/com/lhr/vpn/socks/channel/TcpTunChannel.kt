package com.lhr.vpn.socks.channel

import android.util.Log
import com.lhr.vpn.ext.toHexString
import kotlinx.coroutines.*
import java.net.Socket

/**
 * @author lhr
 * @date 29/10/2022
 * @des tcp tun channel
 */
class TcpTunChannel(private val socket: Socket, private val name: String): BaseTunChannel<ByteArray, ByteArray>() {
    private val tag = this::class.java.simpleName + ":" + socket.localPort

    @Volatile
    var isValid = true
        private set

    val isInputChannelOpened get() = inputJob?.isActive == true

    val isOutputChannelOpened get() = outputJob?.isActive == true

    private var inputJob: Job? = null

    private var outputJob: Job? = null

    fun openChannel(scope: CoroutineScope = GlobalScope){
        if (!isInputChannelOpened){
            openInputChannel(scope)
        }

        if (!isOutputChannelOpened){
            openOutputChannel(scope)
        }
    }

    fun openInputChannel(scope: CoroutineScope = GlobalScope){
        if (socket.isInputShutdown) {
            throw RuntimeException("socket input is shutdown")
        }
        inputJob?.cancel()
        inputJob = null

        inputJob = scope.launch(Dispatchers.IO) {
            val receiveData = ByteArray(1460)
            val inputStream = socket.getInputStream()
            kotlin.runCatching {
                while (isActive){
                    val len = inputStream.read(receiveData)
                    if (len == -1){
                        Log.e(tag, "$name socket input is close")
                        break
                    }
                    if (len > 0){
                        val data = ByteArray(len)
                        System.arraycopy(receiveData, 0, data, 0, len)
                        receivePool.addLast(data)
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
            send(CloseSign)
            Log.d(tag, "$name input over")
            waitClose()
        }
    }

    fun openOutputChannel(scope: CoroutineScope = GlobalScope){
        if (socket.isOutputShutdown) {
            throw RuntimeException("$name socket output is shutdown")
        }
        outputJob?.cancel()
        outputJob = null

        outputJob = scope.launch(Dispatchers.IO){
            val output = socket.getOutputStream()
            kotlin.runCatching {
                while (isActive){
                    val sendData = sendPool.takeFirst()
                    if (socket.isOutputShutdown){
                        Log.e(tag, "$name socket output is close")
                        break
                    }
                    if (sendData === CloseSign){
                        //empty data , close output
                        Log.e(tag, "$name shutdown socket output")
                        socket.shutdownOutput()
                        socket.shutdownInput()
                        break
                    }
                    Log.e(tag, "$name socket write ${sendData.toHexString()}")
                    output.write(sendData)
                }
            }.onFailure {
                it.printStackTrace()
            }
            Log.d(tag, "$name output over")
            waitClose()
        }
    }


    fun waitClose(){
        if (!isConnected()){
            Log.d(tag, "$name socket disconnect")
            onClose()
        }
    }

    fun closeInputChannel(){
        if (!socket.isInputShutdown){
            socket.shutdownInput()
        }
        inputJob?.cancel()
        inputJob = null
    }

    fun closeOutputChannel(){
        if (!socket.isOutputShutdown){
            socket.shutdownOutput()
        }
        outputJob?.cancel()
        outputJob = null
    }

    fun isConnected(): Boolean{
        kotlin.runCatching {
            socket.sendUrgentData(0xFF)
            return true
        }
        return false
    }

    private fun onClose() {
        isValid = false
        closeInputChannel()
        closeOutputChannel()
        socket.close()
        receivePool.addLast(CloseSign)
    }
}