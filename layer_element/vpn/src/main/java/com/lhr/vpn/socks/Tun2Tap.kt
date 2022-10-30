package com.lhr.vpn.socks

import android.os.ParcelFileDescriptor
import android.util.Log
import com.lhr.vpn.socks.net.MAX_PACKET_SIZE
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileChannel

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: 虚拟网卡设备
 */
class Tun2Tap(private val tunInterface: ParcelFileDescriptor){
    private val tag = this::class.java.simpleName

    private val tunOutput by lazy { FileOutputStream(tunInterface.fileDescriptor) }

    private val tunInput by lazy { FileInputStream(tunInterface.fileDescriptor) }

    private val buffer: ByteBuffer by lazy { ByteBuffer.allocate(MAX_PACKET_SIZE) }

    val isInputShutdown: Boolean get() {
        return isStreamClosed(tunInput.channel)
    }

    val isOutputShutdown: Boolean get() {
        return isStreamClosed(tunOutput.channel)
    }

    /**
     * read ip packet data
     */
    fun readTun(): ByteArray {
        //读取数据
        synchronized(tunInput){
            while (true){
                Thread.sleep(1)
                val len = tunInput.read(buffer.array())
                if (len <= 0){
                    continue
                }
                buffer.flip()
                buffer.limit(len)
                val data = ByteArray(len)
                buffer.get(data)
//                Log.d(tag, "虚拟网卡读取<<<${data.size}byte")
                buffer.clear()
                return data
            }
        }
    }

    /**
     * write ip packet data
     */
    fun writeTun(data: ByteArray) {
        //写入数据
        synchronized(tunOutput){
//            Log.d(tag, "虚拟网卡写入>>>${data.size}byte")
            tunOutput.write(data)
        }
    }

    fun close(){
        kotlin.runCatching {
            tunInput.close()
        }
        kotlin.runCatching {
            tunOutput.close()
        }
    }

    private fun isStreamClosed(fc: FileChannel): Boolean{
        try {
            return fc.position() >= 0L // This may throw a ClosedChannelException.
        } catch (cce: ClosedChannelException) {
            return false
        } catch (e: IOException) {
        }
        return true
    }
}