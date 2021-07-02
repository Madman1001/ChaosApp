package com.example.access

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(
                this,
                AccessibilityTestService::class.java.name
            )
        ) { // 判断服务是否开启
            OpenAccessibilitySettingHelper.jumpToSettingPage(this) // 跳转到开启页面
        } else {
            Toast.makeText(this, "服务已开启",Toast.LENGTH_LONG)
            //do other things...
        }
    }
}