package com.lhr.vpn.socks.proxy.tcp

import android.util.Log
import com.lhr.vpn.socks.Tunnel
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @author lhr
 * @date 31/10/2022
 * @des
 */
class TcpConnection(
    private val remoteChannel: Tunnel,
    private val scope: CoroutineScope = MainScope()
) {
    private val TAG = this::class.java.simpleName
    private val selector = Selector.open()
    private val buf = ByteBuffer.allocate(1024 * 4)
    private val remoteQueue: ConcurrentLinkedQueue<ByteBuffer> = ConcurrentLinkedQueue()
    private val localQueue: ConcurrentLinkedQueue<ByteBuffer> = ConcurrentLinkedQueue()

    private var workJob: Job? = null

    fun startWork() {
        remoteChannel.channel.register(selector, SelectionKey.OP_READ, remoteChannel)
        remoteChannel.bindChannel?.channel?.register(
            selector,
            SelectionKey.OP_READ,
            remoteChannel.bindChannel
        )
        workJob = scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                while (isActive) {
                    selector.select()
                    val iterator = selector.selectedKeys().iterator()
                    while (iterator.hasNext()) {
                        val key = iterator.next()
                        if (key.isValid && key.isReadable){
                            onRead(key)
                        }
                        if (key.isValid && key.isWritable){
                            onWrite(key)
                        }
                        iterator.remove()
                    }
                    val keys = selector.keys()
                    if (keys.isEmpty() || keys.all { !it.isValid }){
                        break
                    }
                }
            }.onFailure {
                Log.e(TAG, "work job stop", it)
            }
            remoteChannel.bindChannel?.channel?.use { }
            remoteChannel.channel.use { }
            selector.use { }
            Log.e(TAG, "work job end")
        }
    }

    fun stopWork() {
        workJob?.cancel()
    }

    private fun onWrite(selectionKey: SelectionKey) {
        val tunnel = selectionKey.attachment() as Tunnel
        Log.d(TAG, "WRITE ${if (tunnel === remoteChannel) "remote" else "local"}")
        val queue = if (tunnel === remoteChannel) {
            remoteQueue
        } else {
            localQueue
        }
        if (queue.isEmpty()){
            selectionKey.interestOps(SelectionKey.OP_READ)
            return
        }
        val buffer = queue.poll() ?: return
        Log.d(TAG, "WRITE ${String(buf.array(), buf.position(), buf.limit() - buf.position())}")
        while (buffer.hasRemaining() && tunnel.channel.isConnected){
            val len = tunnel.channel.write(buffer)
            if (len <= 0){
                selectionKey.cancel()
                throw IOException("write $len bytes,")
            }
        }
    }

    private fun onRead(selectionKey: SelectionKey) {
        val tunnel = selectionKey.attachment() as Tunnel
        Log.d(TAG, "READ ${if (tunnel === remoteChannel) "remote" else "local"}")
        buf.clear()
        val len = tunnel.channel.read(buf)
        if (len > 0){
            buf.flip()
            Log.d(TAG,"READ ${String(buf.array(), buf.position(), buf.limit() - buf.position())}")
            val queue = if (tunnel === remoteChannel) {
                localQueue
            } else {
                remoteQueue
            }
            if (queue.isEmpty()){
                tunnel.bindChannel?.channel?.keyFor(selector)?.interestOps(SelectionKey.OP_READ or SelectionKey.OP_WRITE)
            }
            queue.offer(buf.duplicate())
        } else {
            selectionKey.cancel()
            Log.e(TAG, "tcp is over ${if (tunnel === remoteChannel) "remote" else "local"}")
        }
    }
}