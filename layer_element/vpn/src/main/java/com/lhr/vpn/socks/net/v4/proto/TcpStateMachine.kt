package com.lhr.vpn.socks.net.v4.proto

import com.lhr.vpn.socks.net.v4.*

/**
 * @author lhr
 * @date 15/10/2022
 * @des tcp状态机
 */
object TcpStateMachine {

    fun recvSign(){

    }

    fun sendSign(){

    }

    fun createTcpStateMap(): Map<Int, TcpState> {
        val result = mutableMapOf<Int, TcpState>()

        val closed = TcpState(
            STATE_CLOSED,
            listOf(
                TcpStateRoute(STATE_SYN_SENT, SIGN_SYN, SIGN_NUL)
            )
        )

        val listen = TcpState(
            STATE_LISTEN,
            listOf(
                TcpStateRoute(STATE_SYN_REVD, SIGN_SYN or SIGN_ACK, SIGN_SYN)
            )
        )

        val synRevd = TcpState(
            STATE_SYN_REVD,
            listOf(
                TcpStateRoute(STATE_FIN_WAIT_1, SIGN_FIN, SIGN_NUL),
                TcpStateRoute(STATE_ESTABLISHED, SIGN_NUL, SIGN_ACK),
                TcpStateRoute(STATE_LISTEN, SIGN_NUL, SIGN_RST)
            )
        )

        val synSent = TcpState(
            STATE_SYN_SENT,
            listOf(
                TcpStateRoute(STATE_SYN_REVD, SIGN_SYN, SIGN_SYN or SIGN_ACK),
                TcpStateRoute(STATE_ESTABLISHED, SIGN_ACK, SIGN_SYN or SIGN_ACK)
            )
        )

        val established = TcpState(
            STATE_ESTABLISHED,
            listOf(
                TcpStateRoute(STATE_FIN_WAIT_1, SIGN_FIN, SIGN_NUL),
                TcpStateRoute(STATE_CLOSE_WAIT, SIGN_ACK, SIGN_FIN)
            )
        )

        val finWait1 = TcpState(
            STATE_FIN_WAIT_1,
            listOf(
                TcpStateRoute(STATE_TIME_WAIT, SIGN_ACK, SIGN_FIN or SIGN_ACK),
                TcpStateRoute(STATE_CLOSING, SIGN_ACK, SIGN_FIN),
                TcpStateRoute(STATE_FIN_WAIT_2, SIGN_NUL, SIGN_ACK),
            )
        )

        val finWait2 = TcpState(
            STATE_FIN_WAIT_2,
            listOf(
                TcpStateRoute(STATE_TIME_WAIT, SIGN_ACK, SIGN_FIN),
            )
        )

        val closing = TcpState(
            STATE_CLOSING,
            listOf(
                TcpStateRoute(STATE_TIME_WAIT, SIGN_NUL, SIGN_ACK)
            )
        )

        val closeWait = TcpState(
            STATE_CLOSE_WAIT,
            listOf(
                TcpStateRoute(STATE_LAST_ACK, SIGN_FIN, SIGN_NUL)
            )
        )

        val lastAck = TcpState(
            STATE_LAST_ACK,
            listOf(
                TcpStateRoute(STATE_CLOSED, SIGN_NUL, SIGN_ACK)
            )
        )

        val timeWait = TcpState(
            STATE_TIME_WAIT,
            listOf()
        )

        result.putAll(
            mapOf(
                STATE_CLOSED to closed,
                STATE_LISTEN to listen,
                STATE_SYN_REVD to synRevd,
                STATE_SYN_SENT to synSent,
                STATE_ESTABLISHED to established,
                STATE_FIN_WAIT_1 to finWait1,
                STATE_FIN_WAIT_2 to finWait2,
                STATE_CLOSING to closing,
                STATE_TIME_WAIT to timeWait,
                STATE_CLOSE_WAIT to closeWait,
                STATE_LAST_ACK to lastAck,
            )
        )
        return result
    }

}