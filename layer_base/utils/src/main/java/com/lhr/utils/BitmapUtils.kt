package com.lhr.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log

/**
 * @author lhr
 * @date 2021/8/19
 * @des
 */
object BitmapUtils {
    private val Tag = BitmapUtils::class.java.simpleName

    fun getBitmapFromFile(path: String?, reqWidth: Int = -1, reqHeight: Int = -1): Bitmap?{
        var bitmap: Bitmap? = null
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path,option)
        option.inSampleSize = calculateInSampleSize(option, reqWidth, reqHeight)
        Log.e(Tag,"bitmap sample size ${option.inSampleSize}")
        option.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeFile(path,option)
        return bitmap
    }

    fun cropBitmapToRect(bitmap: Bitmap, rect: Rect): Bitmap{
        val oldWidth = bitmap.width
        val oldHeight = bitmap.height

        Log.e(Tag,"bitmap ${oldWidth}x${oldHeight}  ${rect.width()}x${rect.height()}")
        return Bitmap.createBitmap(bitmap,rect.left, rect.top, rect.width(), rect.height())
    }

    private fun calculateInSampleSize(option: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int{
        val scale = 1.0
        if (reqHeight <= 0 || reqWidth <= 0){
            return scale.toInt()
        }

        val heightScale = option.outHeight * 1.0 / reqHeight
        val widthScale = option.outWidth * 1.0 / reqWidth

        return if (heightScale > widthScale){
            widthScale.toInt()
        }else{
            heightScale.toInt()
        }
    }
}