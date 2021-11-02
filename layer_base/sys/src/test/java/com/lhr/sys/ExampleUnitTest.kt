package com.lhr.sys

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
object ExampleUnitTest {

    @JvmStatic fun main(arge: Array<String>) {
        val runnerInt = ExampleUnitTest::class.java.getMethod("getInt")

        val runnerBoolean = ExampleUnitTest::class.java.getMethod("getBoolean")

        val runnerChar = ExampleUnitTest::class.java.getMethod("getChar")

        val runnerString = ExampleUnitTest::class.java.getMethod("getString")

        val runnerFloat = ExampleUnitTest::class.java.getMethod("getFloat")

        val runnerVoid = ExampleUnitTest::class.java.getMethod("getVoid")

        println(runnerInt.name + ": " + runnerInt.returnType)
        println(runnerBoolean.name + ": " + runnerBoolean.returnType)
        println(runnerChar.name + ": " + runnerChar.returnType)
        println(runnerString.name + ": " + runnerString.returnType)
        println(runnerFloat.name + ": " + runnerFloat.returnType)
        println(runnerVoid.name + ": " + runnerVoid.returnType)
    }

    fun getBoolean(): Boolean{
        return false
    }

    fun getChar(): Char{
        return '0'
    }

    fun getFloat(): Float{
        return 1.0f
    }

    fun getInt(): Int{
        return 0
    }

    fun getString(): String{
        return ""
    }

    fun getVoid(){

    }
}