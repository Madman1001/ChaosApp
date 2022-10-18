package com.lhr.vpn.socks.net.v4.proto

/**
 * @author lhr
 * @date 16/10/2022
 * @des tcp状态
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
