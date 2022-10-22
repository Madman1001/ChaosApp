package com.lhr.vpn.socks

import android.os.ParcelFileDescriptor
import android.util.Log
import com.lhr.vpn.socks.net.v4.NetV4Protocol
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: 虚拟网卡设备
 */
class Tun2Tap(private val tunInterface: ParcelFileDescriptor){
    private val tag = this::class.java.simpleName

    private val tunOutput: FileOutputStream by lazy { FileOutputStream(tunInterface.fileDescriptor) }

    private val tunInput: FileInputStream by lazy { FileInputStream(tunInterface.fileDescriptor) }

    private val buffer: ByteBuffer by lazy { ByteBuffer.allocate(NetV4Protocol.MAX_PACKET_SIZE) }

    /**
     * read ip packet data
     */
    fun readTun(): ByteArray {
        //读取数据
        var len = -1
        kotlin.runCatching {
            synchronized(buffer){
                while (true){
                    Thread.sleep(1)
                    len = tunInput.read(buffer.array())
                    if (len <= 0){
                        continue
                    }
                    buffer.rewind()
                    buffer.limit(len)
                    val data = ByteArray(len)
                    buffer.get(data)
                    Log.d(tag, "虚拟网卡读取<<<${data.size}byte")
                    buffer.clear()
                    return data
                }
            }
        }

        return ByteArray(0)
    }

    /**
     * write ip packet data
     */
    fun writeTun(data: ByteArray) {
        //写入数据
        synchronized(tunOutput){
            kotlin.runCatching {
                Log.d(tag, "虚拟网卡写入>>>${data.size}byte")
                tunOutput.write(data)
            }
        }
    }
}