package com.lhr.view.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.FileDescriptor

/**
 * @author lhr
 * @date 2021/8/26
 * @des
 */
object MediaUtils {

    /**
     * 获取视频中的一帧画面
     */
    fun getMediaFrame(context: Context, path: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.parse(path))

        return retriever.frameAtTime
    }

    /**
     * 获取视频中的一帧画面
     */
    fun getMediaFrame(fd: FileDescriptor, offset: Long , length: Long): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(fd, offset, length)
        return retriever.frameAtTime
    }
}