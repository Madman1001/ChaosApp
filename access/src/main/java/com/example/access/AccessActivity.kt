package com.example.access

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.access.factory.RomRuleBeanFactory
import com.example.access.utils.PermissionUtils
import com.example.access.factory.RomFeatureBeanFactory

class AccessActivity : AppCompatActivity(),View.OnClickListener {
    private val tag = "AS_${this::class.java.simpleName}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access)

        this.findViewById<View>(R.id.accessibility_window).setOnClickListener(this)
        this.findViewById<View>(R.id.accessibility_sys_setting).setOnClickListener(this)
        this.findViewById<View>(R.id.accessibility_setting).setOnClickListener(this)
        this.findViewById<View>(R.id.accessibility_permission).setOnClickListener(this)
        this.findViewById<View>(R.id.accessibility_access).setOnClickListener(this)

        Thread{
            val code = RomFeatureBeanFactory.getRomCode(this)
            Log.e(tag,code.toString())
            val bean = RomRuleBeanFactory.getRomRuleBean(this)
            Log.e(tag,bean?.toString()?:"")
        }.start()

    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.accessibility_window ->{
                PermissionUtils.gotoWindowEnableSetting(this)
            }
            R.id.accessibility_sys_setting ->{
                PermissionUtils.gotoSystemWrite(this)
            }
            R.id.accessibility_setting ->{
                PermissionUtils.gotoNotificationAccessSetting(this)
            }
            R.id.accessibility_permission ->{
                PermissionUtils.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1024)
            }
            R.id.accessibility_access ->{
                PermissionUtils.gotoAccessibilitySetting(this)
            }
        }
    }
}