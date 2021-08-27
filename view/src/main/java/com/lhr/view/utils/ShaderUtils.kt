package com.lhr.view.utils

import android.opengl.GLES20

/**
 * @author lhr
 * @date 2021/8/27
 * @des
 */
object ShaderUtils {

    fun loadShader(glVertexShader: Int,vertexShader: String) : Int{
        val glCreateShader = GLES20.glCreateShader(glVertexShader)
        GLES20.glShaderSource(glCreateShader, vertexShader)
        GLES20.glCompileShader(glCreateShader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(glCreateShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] != GLES20.GL_TRUE){
            GLES20.glDeleteShader(glCreateShader)
            return -1
        }
        return glCreateShader
    }
}