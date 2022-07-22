package com.lhr.learn.applications

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
@Parcelize
data class AppInfo(
    val name: String,
    val packageName: String,
    val uid: Int,
    val sourceDir: String,
    val nativeLibraryDir: String?,
    val hasNativeLibs: Boolean,
    val appIconUri: Uri,
    var appSize: Long = 0
) : Parcelable
