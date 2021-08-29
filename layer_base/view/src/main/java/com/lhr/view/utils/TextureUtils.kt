package com.lhr.view.utils

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils

/**
 * @author lhr
 * @date 2021/8/27
 * @des 纹理工具
 */
object TextureUtils {

    /**
     * 加载纹理
     */
    fun loadTexture(bitmap: Bitmap): Int{
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1,textureIds,0)

        if (textureIds[0] == 0){
            return 0
        }

        //绑定纹理到GLES
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureIds[0])

        //设置默认的纹理过滤参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)

        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0)

        //生成贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

        //取消纹理绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)

        return textureIds[0]
    }
}