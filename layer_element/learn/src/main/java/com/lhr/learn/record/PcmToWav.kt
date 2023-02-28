package com.lhr.learn.record

import android.text.TextUtils
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description: pcm文件转wav文件
 */
object PcmToWav {
    private val TAG = this::class.java.simpleName

    /**
     * 合并多个pcm文件为一个wav文件
     *
     * @param filePathList    pcm文件路径集合
     * @param destinationPath 目标wav文件路径
     * @return true|false
     */
    fun mergePCMFilesToWAVFile(
        filePathList: List<String?>,
        destinationPath: String,
        deletePcmFile: Boolean
    ): Boolean {
        val files = mutableListOf<File>()
        var buffer: ByteArray? = null
        var TOTAL_SIZE = 0
        val fileNum = filePathList.size
        for (i in 0 until fileNum) {
            val path = filePathList[i] ?: ""
            if (!TextUtils.isEmpty(path)) {
                files.add(File(path))
            }
            TOTAL_SIZE += files[i].length().toInt()
        }

        // 填入参数，比特率等等。这里用的是16位单声道 8000 hz
        val header = WaveHeader()
        // 长度字段 = 内容的大小（TOTAL_SIZE) +
        // 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = TOTAL_SIZE + (44 - 8)
        header.FmtHdrLeth = 16
        header.BitsPerSample = 16
        header.Channels = 1
        header.FormatTag = 0x0001
        header.SamplesPerSec = 8000
        header.BlockAlign = (header.Channels * header.BitsPerSample / 8).toShort()
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec
        header.DataHdrLeth = TOTAL_SIZE

        var h: ByteArray? = null
        h = try {
            header.header
        } catch (e1: IOException) {
            loge(e1.message)
            return false
        }

        h ?: return false
        // WAV标准，头部应该是44字节,如果不是44个字节则不进行转换文件
        if (h.size != 44) return false


        //先删除目标文件
        val destfile = File(destinationPath)
        if (destfile.exists()) destfile.delete()

        //合成所有的pcm文件的数据，写到目标文件
        kotlin.runCatching {
            // Length of All Files, Total Size
            buffer = ByteArray(1024 * 4)
            var inStream: InputStream? = null
            var ouStream: OutputStream? = null
            ouStream = BufferedOutputStream(
                FileOutputStream(
                    destinationPath
                )
            )
            ouStream.write(h, 0, h.size)
            for (j in 0 until fileNum) {
                inStream = BufferedInputStream(FileInputStream(files[j]))
                var size = inStream.read(buffer)
                while (size != -1) {
                    ouStream.write(buffer)
                    size = inStream.read(buffer)
                }
                inStream.close()
            }
            ouStream.close()
        }.onFailure {
            loge(it.message)
            return false
        }

        if (deletePcmFile) {
            clearFiles(filePathList)
        }
        Log.i(
            "PcmToWav",
            "mergePCMFilesToWAVFile  success!" + SimpleDateFormat("yyyy-MM-dd hh:mm").format(
                Date()
            )
        )
        return true
    }

    /**
     * 将一个pcm文件转化为wav文件
     *
     * @param pcmPath         pcm文件路径
     * @param destinationPath 目标文件路径(wav)
     * @param deletePcmFile   是否删除源文件
     * @return
     */
    fun makePCMFileToWAVFile(
        pcmPath: String,
        destinationPath: String,
        deletePcmFile: Boolean
    ): Boolean {
        return mergePCMFilesToWAVFile(listOf(pcmPath), destinationPath, deletePcmFile)
    }

    /**
     * 清除文件
     *
     * @param filePathList
     */
    private fun clearFiles(filePathList: List<String?>) {
        for (i in filePathList.indices) {
            val path = filePathList[i] ?: ""
            if (TextUtils.isEmpty(path)) continue
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun logd(msg: String?) {
        Log.d(TAG, "$msg")
    }

    private fun loge(msg: String?) {
        Log.e(TAG, "$msg")
    }
}