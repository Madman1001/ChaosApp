package com.example.access

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.access.utils.PermissionUtils

class AccessActivity : AppCompatActivity(),View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access)
        if (!PermissionUtils.isAccessibilityEnable(
                this,
                AccessibilityTestService::class.java.name
            )
        ) { // 判断服务是否开启
            PermissionUtils.gotoAccessibilitySetting(this) // 跳转到开启页面
        } else {
            Toast.makeText(this, "服务已开启",Toast.LENGTH_LONG).show()
            //do other things...
        }

        this.findViewById<View>(R.id.accessibility_window).setOnClickListener(this)
        this.findViewById<View>(R.id.accessibility_ring).setOnClickListener(this)
        this.findViewById<View>(R.id.accessibility_setting).setOnClickListener(this)
        this.findViewById<View>(R.id.accessibility_permission).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.accessibility_window ->{
                PermissionUtils.gotoWindowEnableSetting(this)
            }
            R.id.accessibility_ring ->{
                PermissionUtils.gotoRingtoneAdjustEnableSetting(this)
            }
            R.id.accessibility_setting ->{
                PermissionUtils.gotoNotificationAccessSetting(this)
            }
            R.id.accessibility_permission ->{
                PermissionUtils.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1024)
            }
        }
    }
}