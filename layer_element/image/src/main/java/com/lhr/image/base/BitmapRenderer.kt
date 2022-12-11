package com.lhr.learn.bitmap.gl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES10
import android.opengl.GLES20
import com.lhr.common.ext.readText
import com.lhr.learn.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.random.Random


/**
 * @author lhr
 * @date 7/12/2022
 * @des
 */
class BitmapRenderer(val context: Context, var bitmap: Bitmap): GLRenderer() {
    private val TAG = "TestRenderer"
    private var program = 0
    private var vPosition = 0
    private var aTexCoord = 0
    private var uTextureUnit = 0
    private var mTextureId = 0

    private var vertices: FloatBuffer? = null

    private var fragments: FloatBuffer? = null

    /**
     * 获取图形的顶点
     * 特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
     * 转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
     *
     * @return 顶点Buffer
     */
    private fun getVertices(): FloatBuffer {
        val vertices = floatArrayOf(
            -1f, -1f,
            -1f, 1f,
            1f, 1f,
            1f, -1f
        )

        // 创建顶点坐标数据float缓冲
        // vertices.length*4是因为一个float占四个字节
        val vbb: ByteBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
        vbb.order(ByteOrder.nativeOrder()) //设置字节顺序
        val vertexBuf: FloatBuffer = vbb.asFloatBuffer() //转换为Float型缓冲
        vertexBuf.put(vertices) //向缓冲区中放入顶点坐标数据
        vertexBuf.position(0) //设置缓冲区起始位置
        return vertexBuf
    }

    private fun getFragments(): FloatBuffer {
        val fragments = floatArrayOf(
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f
        )

        // 创建贴图坐标数据float缓冲
        val fbb = ByteBuffer.allocateDirect(fragments.size * Float.SIZE_BYTES)
        fbb.order(ByteOrder.nativeOrder())
        val fragmentBuf = fbb.asFloatBuffer()
        fragmentBuf.put(fragments)
        fragmentBuf.position(0)
        return fragmentBuf
    }

    override fun onCreated() {
        //基于顶点着色器与片元着色器创建程序
        program = ShaderUtil.createProgram(verticesShader, fragmentShader)
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord")
        uTextureUnit = GLES20.glGetUniformLocation(program, "uTextureUnit")
        mTextureId = ShaderUtil.loadTexture(bitmap)
        vertices = getVertices()
        fragments = getFragments()
    }

    override fun onUpdate() {}

    override fun onDrawFrame(surface: GLSurface?) {
        // 设置clear color颜色RGBA(这里仅仅是设置清屏时GLES20.glClear()用的颜色值而不是执行清屏)
        GLES20.glClearColor(0.0f, 0f, 0f, 0.0f)

        // 清除深度缓冲与颜色缓冲(清屏,否则会出现绘制之外的区域花屏)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        // 使用某套shader程序
        GLES20.glUseProgram(program)
        vertices?.position(0)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertices)
        // 为画笔指定顶点位置数据(vPosition)
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, fragments)
        // 允许顶点位置数据数组
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glEnableVertexAttribArray(aTexCoord)

        GLES20.glUniform1i(uTextureUnit, 0)
        // 开启纹理透明混合，这样才能绘制透明图片
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 设置当前活动的纹理单元为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        val randR = GLES20.glGetUniformLocation(program, "randR")
        val randG = GLES20.glGetUniformLocation(program, "randG")
        val randB = GLES20.glGetUniformLocation(program, "randB")
        val randA = GLES20.glGetUniformLocation(program, "randA")
        GLES20.glUniform1f(randR, Random.nextFloat())
        GLES20.glUniform1f(randG, Random.nextFloat())
        GLES20.glUniform1f(randB, Random.nextFloat())
        GLES20.glUniform1f(randA, Random.nextFloat())

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLE_FAN,        /* 绘制模式 */
            0,                        /* 顶点起始位置 */
            4     /* 数据长度 */
        )
        // 解绑
        GLES20.glDisableVertexAttribArray(vPosition)
        GLES20.glDisableVertexAttribArray(aTexCoord)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }

    override fun onDestroy() {}

    // 顶点着色器的脚本
    private val verticesShader by lazy {
        context.resources.openRawResource(R.raw.image_v).readText()
    }

    // 片元着色器的脚本
    private val fragmentShader by lazy {
        context.resources.openRawResource(R.raw.image_f).readText()
    }
}