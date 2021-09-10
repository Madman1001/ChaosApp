package com.lhr.view.utils

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

    /**
     * 获取Bitmap对象
     * @param resource 资源对象
     * @param id 资源id
     * @param reqWidth 期望的图片宽度
     * @param reqHeight 期望的图片高度
     */
    fun getBitmapFromResource(resource: Resources, id: Int, reqWidth: Int = -1, reqHeight: Int = -1): Bitmap?{
        var bitmap: Bitmap? = null
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resource,id,option)
        option.inSampleSize =
            calculateInSampleSize(
                option,
                reqWidth,
                reqHeight
            )
        option.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeResource(resource,id,option)
        return bitmap
    }

    /**
     * 获取Bitmap对象
     * @param path 图片文件路径
     * @param reqWidth 期望的图片宽度
     * @param reqHeight 期望的图片高度
     */
    fun getBitmapFromFile(path: String?, reqWidth: Int = -1, reqHeight: Int = -1): Bitmap?{
        var bitmap: Bitmap? = null
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path,option)
        option.inSampleSize =
            calculateInSampleSize(
                option,
                reqWidth,
                reqHeight
            )
        option.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeFile(path,option)
        return bitmap
    }

    /**
     * 计算缩放比例
     */
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