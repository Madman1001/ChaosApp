package com.example.access

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.access.utils.PermissionUtils
import com.lhr.centre.annotation.CElement

@CElement(name = "无障碍功能")
class AccessActivity : AppCompatActivity() {
    private val tag = "AS_${this::class.java.simpleName}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_access)

        this.overridePendingTransition(0,0)
        if (!PermissionUtils.isAccessibilityEnable(this,"com.example.access.AccessibilityTestService")) {
            PermissionUtils.gotoAccessibilitySetting(this)
        }else{
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        this.finish()
    }
}