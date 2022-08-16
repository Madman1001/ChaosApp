package com.lhr.learn.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

/**
 * @CreateDate: 2022/7/1
 * @Author: mac
 * @Description: 安装包扫描工具
 */
object ApkFileUtil {
    private const val LOG_TAG = "ApkFileUtil"

    private val mFilesUri: Uri = MediaStore.Files.getContentUri("external") // 存储卡的Uri

    private val mFilesColumn = arrayOf<String>( // 媒体库的字段名称数组
        MediaStore.Files.FileColumns._ID,  // 编号
        MediaStore.Files.FileColumns.TITLE,  // 标题
        MediaStore.Files.FileColumns.SIZE,  // 文件大小
        MediaStore.Files.FileColumns.DATA,  // 文件路径
        MediaStore.Files.FileColumns.MIME_TYPE // 类型
    )

    /**
     * 获取安装包列表
    */
    fun getApkList(context: Context): List<String>{
        val result = ArrayList<String>()
        // 查找存储卡上所有的apk文件，其中mime_type指定了APK的文件类型，或者判断文件路径是否以.apk结尾
        val cursor: Cursor? = context.contentResolver.query(
            mFilesUri, mFilesColumn,
            "mime_type='application/vnd.android.package-archive' or _data like '%.apk'", null, null
        )
        if (cursor != null) {
            kotlin.runCatching {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(3))
                }
            }
            cursor.close() // 关闭数据库游标
        }
        return result
    }
}