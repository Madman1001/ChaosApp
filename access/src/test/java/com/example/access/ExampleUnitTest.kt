package com.example.access

import com.example.access.utils.IoUtils
import com.example.access.utils.JsonUtils
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val input = FileInputStream(File("src/main/assets/accessconfigs/902.json"))
        val data = IoUtils.stream2String(input)?:""
        val beans = JsonUtils.readJson(data)
        println(beans)

    }
}