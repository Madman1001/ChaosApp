package com.example.access

import com.example.access.action.setting.BaseSettingTask
import com.example.access.utils.IoUtils
import com.example.access.utils.RomMatchUtils
import org.junit.Test

import java.io.File
import java.io.FileInputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        val input = FileInputStream(File("src/main/assets/rom_feature_config.json"))
//        val data = IoUtils.stream2String(input)?:""
//        val beans = AccessJsonUtils.getRomRuleBean(data)
//        println(RomMatchUtils.compareCondition("ge","29","25"))

        println(Integer.toBinaryString(0x40) + " : " + Integer.toBinaryString(0x20))
    }
}