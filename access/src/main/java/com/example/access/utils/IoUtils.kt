package com.example.access.utils

import java.io.*

/**
 * @author lhr
 * @date 2021/7/9
 * @des IO读取工具
 */
object IoUtils {
    @Throws(IOException::class)
    fun stream2String(inputStream: InputStream): String? {
        var stringWriter: StringWriter? = null
        return try {
            var available = inputStream.available()
            if (available <= 0) {
                available = 16
            }
            stringWriter = StringWriter(available)
            try {
                readerToWriter(InputStreamReader(inputStream) as Reader, stringWriter as Writer, 1024)
                val targetString = stringWriter.toString()
                try {
                    stringWriter.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                targetString
            } catch (th: Throwable) {
                try {
                    stringWriter.close()
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
                throw th
            }
        } catch (th2: Throwable) {
            stringWriter?.close()
            stringWriter = null
            throw th2
        }
    }

    @Throws(IOException::class)
    fun readerToWriter(reader: Reader, writer: Writer, i: Int) {
        val cArr = CharArray(i)
        while (true) {
            val read = reader.read(cArr)
            if (read != -1) {
                writer.write(cArr, 0, read)
            } else {
                return
            }
        }
    }

    @Throws(IOException::class)
    fun inputToOutput(inputStream: InputStream, outputStream: OutputStream, bufferSize: Int) {
        val bArr = ByteArray(bufferSize)
        while (true) {
            val read = inputStream.read(bArr)
            if (read != -1) {
                outputStream.write(bArr, 0, read)
            } else {
                return
            }
        }
    }
}