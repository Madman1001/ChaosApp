package com.lhr.vpn.socks.net.v4.proto

/**
 * @author lhr
 * @date 15/10/2022
 * @des 状态切换条件
 */
class TcpStateRoute(
    val targetState: Int,
    val waitSendSign: Int,
    val waitRecvSign: Int
) {
    var isWaitSend = false
    var isWaitRecv = false

    val isWaitSendComplete: Boolean
        get() = (waitSendSign == 0) || !isWaitSend

    val isWaitRecvComplete: Boolean
        get() = (waitRecvSign == 0) || !isWaitRecv

    fun sendSign(sign: Int): Boolean{
        if (waitSendSign == 0) return false
        if (isWaitSend && sign and waitSendSign != 0){
            isWaitSend = false
        }

        if (isWaitSendComplete && isWaitRecvComplete){
            return true
        }
        return false
    }

    fun recvSign(sign: Int): Boolean{
        if (waitRecvSign == 0) return false
        if (isWaitRecv && sign and waitRecvSign != 0){
            isWaitRecv = false
        }

        if (isWaitSendComplete && isWaitRecvComplete){
            return true
        }
        return false
    }

    fun reset(){
        isWaitSend = waitSendSign != 0
        isWaitRecv = waitRecvSign != 0
    }
}