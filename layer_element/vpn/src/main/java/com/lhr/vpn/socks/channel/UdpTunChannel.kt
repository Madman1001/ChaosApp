package com.lhr.vpn.socks.channel

import android.util.Log
import kotlinx.coroutines.*
import java.net.*

/**
 * @author lhr
 * @date 29/10/2022
 * @des udp tun channel
 */
class UdpTunChannel(
    private val datagramSocket: DatagramSocket,
    private val targetAddress: InetSocketAddress
) : BaseTunChannel<ByteArray, ByteArray>() {
    private val tag = this::class.java.simpleName + ":" + this.toString()

    @Volatile
    var isWaitClose = false
        private set

    @Volatile
    var isValid = true
        private set

    private var inputJob: Job? = null

    private var outputJob: Job? = null

    val isInputChannelOpened get() = inputJob?.isActive == true

    val isOutputChannelOpened get() = outputJob?.isActive == true

    fun openChannel(scope: CoroutineScope = GlobalScope) {
        if (!isInputChannelOpened) {
            openInputChannel(scope)
        }

        if (!isOutputChannelOpened) {
            openOutputChannel(scope)
        }
    }

    fun openInputChannel(scope: CoroutineScope = GlobalScope) {
        if (datagramSocket.isClosed) {
            throw RuntimeException("datagramSocket is closed")
        }
        inputJob?.cancel()
        inputJob = null

        inputJob = scope.launch(Dispatchers.IO) {
            val receivePacket = DatagramPacket(ByteArray(1024), 1024)
            while (isActive && !datagramSocket.isClosed) {
                try {
                    datagramSocket.receive(receivePacket)
                } catch (e: SocketTimeoutException) {
                    Log.e(tag, "datagramSocket timeout")
                    waitCloseOutputChannel()
                    break
                } catch (e: SocketException) {
                    waitCloseOutputChannel()
                    e.printStackTrace()
                    break
                }

                val len = receivePacket.length
                if (len > 0
                    && receivePacket.address.hostAddress == targetAddress.hostString
                    && receivePacket.port == targetAddress.port
                ) {
                    val data = ByteArray(len)
                    System.arraycopy(receivePacket.data, 0, data, 0, len)
                    receivePool.addLast(data)
                }
            }
            waitCloseOutputChannel()
        }
    }

    fun openOutputChannel(scope: CoroutineScope = GlobalScope) {
        if (datagramSocket.isClosed) {
            throw RuntimeException("datagramSocket is closed")
        }
        outputJob?.cancel()
        outputJob = null
        outputJob = scope.launch(Dispatchers.IO) {
            val sendPacket = DatagramPacket(byteArrayOf(), 0, targetAddress)
            while (isActive && !datagramSocket.isClosed) {
                val sendData = sendPool.takeFirst()
                if (datagramSocket.isClosed) {
                    Log.e(tag, "datagramSocket is closed")
                    waitCloseInputChannel()
                    break
                }
                if (sendData === CloseSign) {
                    //empty data , close output
                    Log.e(tag, "shutdown datagramSocket output")
                    waitCloseInputChannel()
                    break
                }
                sendPacket.data = sendData
                datagramSocket.send(sendPacket)
            }
        }
    }

    fun waitClose(){
        isWaitClose = true

        if (isOutputChannelOpened){
            waitCloseOutputChannel()
        } else if (isInputChannelOpened){
            waitCloseInputChannel()
        }
    }

    private fun waitCloseInputChannel() {
        if (!isValid) return

        onClose()
    }

    private fun waitCloseOutputChannel() {
        if (!isValid) return

        if (isOutputChannelOpened){
            sendPool.putLast(CloseSign)
        } else{
            onClose()
        }
    }

    fun closeInputChannel() {
        inputJob?.cancel()
        inputJob = null
    }

    fun closeOutputChannel() {
        outputJob?.cancel()
        outputJob = null
    }

    private fun onClose() {
        isValid = false
        closeInputChannel()
        closeOutputChannel()
        datagramSocket.close()
        receivePool.addLast(CloseSign)
    }
}