package com.lhr.view.utils

import android.graphics.Rect
import kotlin.math.floor

/**
 * @author lhr
 * @date 2021/8/23
 * @des 计算工具
 */
object CalculateUtils {

    /**
     * 计算展示的区域
     * @param inRect 输入源区域
     * @param outRect 输出源区域
     */
    fun calculateDisplayRect(inRect: Rect, outRect: Rect): Rect{
        //输出区域的宽高比
        val outScale = (outRect.height() * 1.0f) / (outRect.width() * 1.0f)

        val inWidth = inRect.width()
        val inHeight = inRect.height()

        var newWidth = inWidth
        var newHeight = inHeight
        if (newWidth > newHeight){
            newWidth = (newHeight / outScale).toInt()
            if (newWidth > inWidth){
                newHeight = floor(((inWidth * 1.0f) / (newWidth * 1.0f)) * newHeight).toInt()
                newWidth = inWidth
            }
        }else{
            newHeight = (newWidth * outScale).toInt()
            if (newHeight > inHeight){
                newWidth = floor(((inHeight * 1.0f) / (newHeight * 1.0f)) * newWidth).toInt()
                newHeight = inHeight
            }
        }

        return Rect(0,0,newWidth,newHeight)
    }
}