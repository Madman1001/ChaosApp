package com.lhr.adb.adbshell

import android.util.Base64
import com.cgutman.adblib.AdbBase64

/**
 * @author lhr
 * @date 4/9/2022
 * @des
 */
class AdbClientBase64: AdbBase64{
    override fun encodeToString(data: ByteArray?): String {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }
}