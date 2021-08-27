package com.lhr.view.render

import android.opengl.GLES20
import com.lhr.view.utils.GLESUtils
import java.nio.FloatBuffer

/**
 * @author lhr
 * @date 2021/8/27
 * @des
 */
class PointRender {
    companion object {
        private val VERTEX_SHADER = "" +
                "attribute vec4 a_Position;\n" +
                "void main()\n" +
                "{\n" +
                "    gl_Position = a_Position;\n" +
                "    gl_PointSize = 100.0;\n" +
                "}"
        private val FRAGMENT_SHADER = "" +
                "precision mediump float;\n" +
                "uniform vec4 u_Color;\n" +
                "void main()\n" +
                "{\n" +
                "    gl_FragColor = u_Color;\n" +
                "}"
        private val POINT_DATA = floatArrayOf(0f, 0f)

        private val POSITION_COMPONENT_COUNT = 2
    }

    private val mVertexData: FloatBuffer
    private var mProgramHandle: Int = 0

    init {
        mVertexData = GLESUtils.createFloatBuffer(POINT_DATA)
    }

    fun render(){
        if (mProgramHandle == 0) {
            mProgramHandle = GLESUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        }

        // Select the program.
        GLES20.glUseProgram(mProgramHandle)
        GLESUtils.checkGlError("glUseProgram")

        val mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position")
        val mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Color")
        mVertexData.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT,
            false, 0, mVertexData)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        val random = java.util.Random()
        // 只要持有传递给GL层的Buffer引用，就可以动态改变相关的数据信息
        mVertexData.put(floatArrayOf(0.9f * random.nextFloat() * (if (random.nextFloat() > 0.5f) 1 else -1).toFloat(),
            0.9f * random.nextFloat() * (if (random.nextFloat() > 0.5f) 1 else -1).toFloat()))
        mVertexData.position(0)

        GLES20.glUniform4f(mColorHandle, random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0f)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }
}