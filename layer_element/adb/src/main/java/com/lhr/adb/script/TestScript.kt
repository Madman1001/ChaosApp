package com.lhr.adb.script

import android.os.Build
import android.util.Log
import com.lhr.adb.IScript
import com.lhr.adb.IActuator
import com.lhr.adb.exec.SyncActuator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author lhr
 * @date 2021/11/21
 * @des 开启adb调试脚本
 */
class TestScript : IScript {
    private val tag = "OpenAdbScript"

    private var actuator: IActuator

    override var listener: (String, Boolean, String) -> Unit = { command, result, message ->
        Log.d(tag, "$result : $message")
    }

    init {
        actuator = SyncActuator { command, result, message ->
            listener.invoke(command, result, message)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            actuator.addCommand("/system/bin/sh")
        }else{
            actuator.addCommand("su")
        }
        actuator.addCommand("date")
        actuator.addCommand("date")
        actuator.addCommand("date")
    }

    override fun start() {
        GlobalScope.launch {
            actuator.execCommand()
        }
    }

    override fun stop() {

    }
}