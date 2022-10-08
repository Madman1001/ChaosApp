package com.lhr.centre

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lhr.centre.annotation.CELEMENT_FLAG_LAUNCHER
import com.lhr.centre.element.TableConstant

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        this.overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)

        if (Centre.isInit) {
            gotoRealLauncherActivity()
        } else {
            Centre.setInitCallback(this::gotoRealLauncherActivity)
        }
    }

    private fun gotoRealLauncherActivity() {
        if (!Centre.isInit) return

        var target = Centre.getElementList().find {
                val flag = it.extraMap[TableConstant.EXTRA_FLAG]?.toInt() ?: 0
                flag and CELEMENT_FLAG_LAUNCHER != 0x00
            }
        if (target == null){
            target = Centre.getElementList().find {
                var result = false
                kotlin.runCatching {
                    val clazz = Class.forName(it.extraMap[TableConstant.EXTRA_VALUE]?:"", false, this.classLoader)
                    var superClass = clazz.superclass
                    while (superClass != null){
                        if (superClass == Activity::class.java){
                            result = true
                            break
                        }
                        superClass = superClass.superclass
                    }
                }
                result
            }
        }
        if (target != null){
            val clazz = Class.forName(target.extraMap[TableConstant.EXTRA_VALUE] ?: "")
            this.startActivity(Intent(this, clazz).apply {
                if (intent?.extras != null){
                    this.putExtras(intent?.extras!!)
                }
            })
        }
        this.finish()
    }
}