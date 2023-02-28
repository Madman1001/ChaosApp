package com.lhr.learn.record

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10


/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description: 录音view model
 */
class RecordViewModel : ViewModel() {

    //是否正在监听
    val vmIsRecording = MutableLiveData(false)

    //当前分贝值
    val vmCurrentDbVal = MutableLiveData(0.0)

    //录音对象
    private var mAudioRecord: AudioRecord? = null

    //是否可以获取噪音分贝值
    @Volatile
    var isNoiseWatchRun = false
        private set

    private var savePcmFilesPath = mutableListOf<String>()
    private var saveFileName = ""

    /**
     * 停止获取噪音分贝值
     */
    fun stop() {
        isNoiseWatchRun = false
    }

    /**
     * 监听噪音分贝水平
     */
    fun startNoiseWatch(context: Context){
        if (isNoiseWatchRun) return
        mAudioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT, BUFFER_MIN_SIZE
        )

        mAudioRecord ?: return

        savePcmFilesPath.clear()
        saveFileName = SimpleDateFormat("yyyyMMddhhmmss").format(Date())

        isNoiseWatchRun = true
        vmIsRecording.postValue(true)
        viewModelScope.launch{
            withContext(IO){
                kotlin.runCatching {
                    mAudioRecord?.startRecording()
                    val bufferSize = Short.SIZE_BYTES * BUFFER_MIN_SIZE
                    val buffer = ByteArray(bufferSize)

                    var lastBigDbTime = 0L
                    var fos: FileOutputStream? = null
                    while (isNoiseWatchRun) {
                        val readsize = mAudioRecord?.read(buffer, 0, bufferSize) ?: 1
                        if (AudioRecord.ERROR_INVALID_OPERATION == readsize) continue

                        //计算分贝大小
                        val volume = calculateDbVal(buffer, readsize)

                        vmCurrentDbVal.postValue(volume)

                        //大于40分贝就开始记录
                        if (volume > 40){
                            lastBigDbTime = System.currentTimeMillis()
                            if (fos == null){
                                // 建立一个可存取字节的文件
                                val fileName = saveFileName + savePcmFilesPath.size
                                val pcmFile = createPcmFile(context, fileName)
                                savePcmFilesPath.add(pcmFile.absolutePath)
                                if (pcmFile.exists()){
                                    pcmFile.delete()
                                }
                                fos = FileOutputStream(pcmFile)
                            }
                        } else {
                            //结束写入
                            if (fos != null
                                && System.currentTimeMillis() - lastBigDbTime > 2000){
                                closeStream(fos)
                                fos = null
                            }
                        }
                        //写入数据
                        kotlin.runCatching {
                            fos?.write(buffer)
                        }.onFailure {
                            it.printStackTrace()
                            log("${it.message}")
                        }
                    }
                    closeStream(fos)
                    mAudioRecord?.stop()
                    mAudioRecord?.release()
                    mAudioRecord = null

                    //将pcm文件转换成wav文件
                    if (savePcmFilesPath.isNotEmpty()){
                        for (i in 0 until savePcmFilesPath.size){
                            val wavFile = createWavFile(context, saveFileName + i)
                            PcmToWav.makePCMFileToWAVFile(savePcmFilesPath[i], wavFile.absolutePath, true)
                        }
                    }

                    vmIsRecording.postValue(false)
                }.onFailure {
                    it.printStackTrace()
                    log("${it.message}")
                }
            }
        }
    }

    /**
     * 计算声音分贝大小
     */
    private fun calculateDbVal(data: ByteArray, len: Int): Double {
        val size = len / Short.SIZE_BYTES
        val buffer = ByteBuffer.wrap(data)
        buffer.order(ByteOrder.nativeOrder())
        var v: Long = 0
        for (i in 0 until size){
            val s = buffer.short
            v += (s * s).toLong()
        }
        val mean = v / len.toDouble()
        return 10 * log10(mean)
    }

    private fun createPcmFile(context: Context, fileName: String): File{
        val cachePath = context.cacheDir
        val rootFile = File(cachePath, PcmBasePath)
        //创建目录
        if (!rootFile.exists()) {
            rootFile.mkdirs()
        }
        val file = if (fileName.endsWith(".pcm")){
            File(rootFile, fileName)
        } else {
            File(rootFile, "$fileName.pcm")
        }
        return file
    }

    private fun createWavFile(context: Context, fileName: String): File{
        val cachePath = context.cacheDir
        val rootFile = File(cachePath, WAVBasePath)
        //创建目录
        if (!rootFile.exists()) {
            rootFile.mkdirs()
        }
        val file = if (fileName.endsWith(".wav")){
            File(rootFile, fileName)
        } else {
            File(rootFile, "$fileName.wav")
        }
        return file
    }

    private fun closeStream(stream: Closeable?){
        stream ?: return
        kotlin.runCatching {
            stream.close()
        }.onFailure {
            it.printStackTrace()
            log("${it.message}")
        }
    }

    companion object{
        private const val WAVBasePath = "/record/wav/"
        private const val PcmBasePath = "/record/pcm/"

        //赫兹采样率
        private const val SAMPLE_RATE_IN_HZ = 8000
        //最小字节缓冲大小
        private val BUFFER_MIN_SIZE by lazy {
            AudioRecord.getMinBufferSize(
                SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT)
        }

        private val TAG = this::class.java.simpleName

        fun log(msg: String?){
            Log.e(TAG, "$msg")
        }
    }
}