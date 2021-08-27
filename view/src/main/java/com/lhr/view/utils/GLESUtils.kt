package com.lhr.view.utils

import android.graphics.Bitmap
import android.opengl.GLES20
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import com.lhr.view.gles.BitmapSurface
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * @author lhr
 * @date 2021/8/27
 * @des
 */
object GLESUtils {
    private val tag = GLESUtils::class.java.simpleName
    private const val SIZEOF_FLOAT = 4

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun clearSurfaceBuffer(surface: Surface) {
        val clearSurface = BitmapSurface(surface,-1,-1)
        clearSurface.makeCurrentSurface()
        clearSurface.clearSurfaceBuffer()
        clearSurface.release()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun drawBitmapToSurface(surface: Surface, bitmap: Bitmap, width: Int, height: Int){
        val bitmapSurface = BitmapSurface(surface,width, height)
        bitmapSurface.makeCurrentSurface()
        bitmapSurface.drawTexture(TextureUtils.loadTexture(bitmap))
        bitmapSurface.release()
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        val msg = op + ": glError 0x" + Integer.toHexString(error)
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(tag, msg)
            throw RuntimeException(msg)
        }else{
            Log.e(tag, op + ": glSuccess 0x" + Integer.toHexString(error))
        }
    }

    /**
     * 创建渲染程序
     */
    fun createProgram(vertexSource: String, fragmentSource: String): Int{
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        checkGlError("glCreateProgram")
        if (program == 0) {
            Log.e(tag, "Could not create program")
        }
        GLES20.glAttachShader(program, vertexShader)
        checkGlError("glAttachShader")
        GLES20.glAttachShader(program, pixelShader)
        checkGlError("glAttachShader")
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(tag, "Could not link program: ")
            Log.e(tag, " " + GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            program = 0
        }
        return program
    }

    /**
     * Allocates a direct float buffer, and populates it with the float array data.
     */
    fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        val bb =
            ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }

    /**
     * Compiles the provided shader source.
     *
     * @return A handle to the shader, or 0 on failure.
     */
    fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        checkGlError("glCreateShader type=$shaderType")
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(tag, "Could not compile shader $shaderType:")
            Log.e(tag, " " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    fun sendImage(width: Int, height: Int) {
        val rgbaBuf = ByteBuffer.allocateDirect(width * height * 4)
        rgbaBuf.position(0)
        val start = System.nanoTime()
        GLES20.glReadPixels(
            0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
            rgbaBuf
        )
        val end = System.nanoTime()
        Log.d(tag, "glReadPixels: " + (end - start))
        saveRgb2Bitmap(
            rgbaBuf, Environment.getExternalStorageDirectory().absolutePath
                .toString() + "/gl_dump_" + width + "_" + height + ".png", width, height
        )
    }

    fun saveRgb2Bitmap(
        buf: Buffer,
        filename: String,
        width: Int,
        height: Int
    ) {
        Log.e(tag, "Creating $filename")
        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(FileOutputStream(filename))
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(buf)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos)
            bmp.recycle()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (bos != null) {
                try {
                    bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}