package com.lhr.view

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * @author lhr
 * @date 2021/8/19
 * @des
 */
object BitmapUtils {
    private val Tag = BitmapUtils::class.java.simpleName

    fun getBitmapFromResource(resource: Resources, id: Int, reqWidth: Int = -1, reqHeight: Int = -1): Bitmap?{
        var bitmap: Bitmap? = null
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resource,id,option)
        option.inSampleSize = calculateInSampleSize(option, reqWidth, reqHeight)
        option.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeResource(resource,id,option)
        return bitmap
    }

    fun getBitmapFromFile(path: String?, reqWidth: Int = -1, reqHeight: Int = -1): Bitmap?{
        var bitmap: Bitmap? = null
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path,option)
        option.inSampleSize = calculateInSampleSize(option, reqWidth, reqHeight)
        option.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeFile(path,option)
        return bitmap
    }

    fun cropBitmapToScreen(bitmap: Bitmap, screenWidth: Int, screenHeight: Int): Bitmap?{
        val oldWidth = bitmap.width
        val oldHeight = bitmap.height

        val scale = (screenWidth * 1.0f) / (screenHeight * 1.0f)

        var newWidth = oldWidth
        var newHeight = oldHeight

        if (oldWidth >= oldHeight){
            newWidth = (newHeight * scale).toInt()
            if (newWidth > oldWidth){
                newWidth = oldWidth
            }
        } else{
            newHeight = (newWidth / scale).toInt()
            if (newHeight > oldHeight){
                newHeight = oldHeight
            }
        }
        return Bitmap.createBitmap(bitmap,(oldWidth - newWidth) / 2, (oldHeight - newHeight) / 2, newWidth, newHeight)
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