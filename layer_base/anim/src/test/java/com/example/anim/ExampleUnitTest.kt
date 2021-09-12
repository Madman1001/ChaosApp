package com.example.anim

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val rawVersion = "V11.2.0"
        var version = ""
        for (index in rawVersion.indices){
            if (rawVersion[index] in '1'..'9'){
                version = rawVersion.substring(index)
                break
            }
        }

        println(version)
    }
}