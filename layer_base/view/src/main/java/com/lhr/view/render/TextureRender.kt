package com.lhr.view.render

import android.opengl.GLES20
import android.opengl.Matrix
import com.lhr.view.utils.GLESUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

/**
 * @author lhr
 * @date 2021/8/27
 * @des
 */
class TextureRender(val textureId: Int, val width: Int, val height: Int) {
    companion object {
        private val VERTEX_SHADER = """
                uniform mat4 u_Matrix;
                attribute vec4 a_Position;
                attribute vec2 a_TexCoord;
                varying vec2 v_TexCoord;
                void main() {
                    v_TexCoord = a_TexCoord;
                    gl_Position = u_Matrix * a_Position;
                }
        """
        private val FRAGMENT_SHADER = """
                precision mediump float;
                varying vec2 v_TexCoord;
                uniform sampler2D u_TextureUnit;
                void main() {
                    gl_FragColor = texture2D(u_TextureUnit, v_TexCoord);
                }
                """

        private val POSITION_COMPONENT_COUNT = 2

        /**
         * 顶点坐标
         */
        private val VERTEX_DATA = floatArrayOf(
            -1f, -1f,
            -1f, 1f,
            1f, 1f,
            1f, -1f
        )

        /**
         * 纹理坐标
         */
        private val TEX_VERTEX = floatArrayOf(
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f)

        /**
         * 纹理坐标中每个点占的向量个数
         */
        private val TEX_VERTEX_COMPONENT_COUNT = 2
    }

    private val mVertexBuffer: FloatBuffer

    private val mTexVertexBuffer: FloatBuffer

    private var mProgramHandle: Int = 0
    private val mProjectionMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

    init {
        mVertexBuffer = GLESUtils.createFloatBuffer(VERTEX_DATA)

        mTexVertexBuffer = GLESUtils.createFloatBuffer(TEX_VERTEX)
    }

    fun render(){
        if (mProgramHandle == 0) {
            mProgramHandle = GLESUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        }

        // Select the program.
        GLES20.glUseProgram(mProgramHandle)
        GLESUtils.checkGlError("glUseProgram")

        val mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position")
        val mMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Matrix")
        // 纹理坐标索引
        val aTexCoordLocation = GLES20.glGetAttribLocation(mProgramHandle,"a_TexCoord")
        val uTextureUnitLocation =  GLES20.glGetUniformLocation(mProgramHandle,"u_TextureUnit")

        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mProjectionMatrix, 0)

        // 加载纹理坐标
        mTexVertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aTexCoordLocation, TEX_VERTEX_COMPONENT_COUNT, GLES20.GL_FLOAT, false, 0, mTexVertexBuffer)
        GLES20.glEnableVertexAttribArray(aTexCoordLocation)

        // 开启纹理透明混合，这样才能绘制透明图片
        GLES20.glEnable(GL10.GL_BLEND)
        GLES20.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)

        mVertexBuffer.position(0)
        GLES20.glVertexAttribPointer(
            mPositionHandle, POSITION_COMPONENT_COUNT,
            GLES20.GL_FLOAT, false, 0, mVertexBuffer
        )
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // 设置当前活动的纹理单元为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId)
        GLES20.glUniform1i(uTextureUnitLocation, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_DATA.size / POSITION_COMPONENT_COUNT)

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }
}