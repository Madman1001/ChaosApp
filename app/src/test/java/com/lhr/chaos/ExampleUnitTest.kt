package com.lhr.chaos

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun test() {
        val runnerInt = ExampleUnitTest::class.java.getMethod("getInt")

        val runnerBoolean = ExampleUnitTest::class.java.getMethod("getBoolean")

        val runnerChar = ExampleUnitTest::class.java.getMethod("getChar")

        val runnerString = ExampleUnitTest::class.java.getMethod("getString")

        val runnerFloat = ExampleUnitTest::class.java.getMethod("getFloat")

        val runnerVoid = ExampleUnitTest::class.java.getMethod("getVoid")

        println(runnerInt.name + ": " + runnerInt.returnType + " " + (runnerInt.returnType == Int::class.java))
        println(runnerBoolean.name + ": " + runnerBoolean.returnType+ " " + (runnerBoolean.returnType == Boolean::class.java))
        println(runnerChar.name + ": " + runnerChar.returnType+ " " + (runnerChar.returnType == Char::class.java))
        println(runnerString.name + ": " + runnerString.returnType+ " " + (runnerString.returnType == String::class.java))
        println(runnerFloat.name + ": " + runnerFloat.returnType+ " " + (runnerFloat.returnType == Float::class.java))
        println(runnerVoid.name + ": " + runnerVoid.returnType+ " " + (runnerVoid.returnType == Any::class.java))
    }


    fun getBoolean(): Boolean{
        return false
    }

    fun getChar(): Char{
        return Char.MIN_VALUE
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
