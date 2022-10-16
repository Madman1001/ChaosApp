package com.lhr.vpn.socks.net.v4.proto

/**
 * @author lhr
 * @date 15/10/2022
 * @des tcp状态机
 */
//初始状态，该状态并不存在
const val STATE_CLOSED = 0

//等待来自远程TCP应用程序的请求
const val STATE_LISTEN = 1

//该端点已经接收到连接请求并发送确认。该端点正在等待最终确认。TCP第二次握手后服务端所处的状态
const val STATE_SYN_REVD = 2

//发送连接请求后等待来自远程端点的确认。TCP第一次握手后客户端所处的状态
const val STATE_SYN_SENT = 3

//代表连接已经建立起来了。这是连接数据传输阶段的正常状态
const val STATE_ESTABLISHED = 4

//等待来自远程TCP的终止连接请求或终止请求的确认
const val STATE_FIN_WAIT_1 = 5

//在此端点发送终止连接请求后，等待来自远程TCP的连接终止请求
const val STATE_FIN_WAIT_2 = 6

//等待来自远程TCP的连接终止请求确认
const val STATE_CLOSING = 7

//等待足够的时间来确保远程TCP接收到其连接终止请求的确认
const val STATE_TIME_WAIT = 8

//该端点已经收到来自远程端点的关闭请求，此TCP正在等待本地应用程序的连接终止请求
const val STATE_CLOSE_WAIT = 9

//等待先前发送到远程TCP的连接终止请求的确认
const val STATE_LAST_ACK = 10

//(0 0 URG ACK PSH RST SYN FIN)
const val SIGN_NUL = 0x00
const val SIGN_URG = 0x20
const val SIGN_ACK = 0x10
const val SIGN_PSH = 0x08
const val SIGN_RST = 0x04
const val SIGN_SYN = 0x02
const val SIGN_FIN = 0x01

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
            TcpStateRoute(STATE_ESTABLISHED, SIGN_ACK, SIGN_NUL),
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

    val  finWait2 = TcpState(
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

class TcpStateMachine(isServer: Boolean) {
    private val stateMap: Map<Int, TcpState> = createTcpStateMap()

    private var currentState = if (isServer) stateMap[STATE_LISTEN] else stateMap[STATE_CLOSED]
        set(value) {
            field = value
            field?.routes?.forEach {
                it.reset()
            }
        }

    fun sendSign(sign: Int) {
        val state = currentState ?: return
        for (route in state.routes) {
            if (route.sendSign(sign)){
                currentState = stateMap[route.targetState]
                break
            }
        }
    }

    fun recvSign(sign: Int) {
        val state = currentState ?: return
        for (route in state.routes) {
            if (route.recvSign(sign)){
                currentState = stateMap[route.targetState]
                break
            }
        }
    }
}