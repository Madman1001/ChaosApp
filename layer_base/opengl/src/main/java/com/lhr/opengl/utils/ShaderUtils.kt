package com.lhr.opengl.utils

import android.content.Context
import android.opengl.GLES20
import androidx.annotation.RawRes
import java.io.InputStream

/**
 * @author lhr
 * @date 2021/8/27
 * @des 着实器程序读取工具
 */
object ShaderUtils {

    fun loadShader(glShaderHandle: Int, context: Context, @RawRes resourceId: Int): Int {
        kotlin.runCatching {
            val sourceInput = context.resources.openRawResource(resourceId)
            loadShader(glShaderHandle, sourceInput)
        }.onSuccess {
            return it
        }.onFailure {
            it.printStackTrace()
        }
        return -1
    }

    fun loadShader(glShaderHandle: Int, vertexShaderInput: InputStream): Int {
        val sourceString = vertexShaderInput.bufferedReader().readText()
        return loadShader(glShaderHandle, sourceString)
    }

    fun loadShader(glShaderHandle: Int, shaderSource: String): Int {
        val glCreateShader = GLES20.glCreateShader(glShaderHandle)
        GLES20.glShaderSource(glCreateShader, shaderSource)
        GLES20.glCompileShader(glCreateShader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(glCreateShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteShader(glCreateShader)
            return -1
        }
        return glCreateShader
    }

    fun readShader(context: Context, @RawRes resourceId: Int): String {
        val sourceInput = context.resources.openRawResource(resourceId)
        return sourceInput.bufferedReader().readText()
    }
}