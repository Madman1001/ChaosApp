package com.lhr.adb.script

import android.os.Build
import android.util.Log
import com.lhr.adb.exec.DefaultActuator
import com.lhr.adb.exec.IActuator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author lhr
 * @date 2021/11/22
 * @des 查看ip地址
 */
class ShowIpScript : IScript{
    private val tag = "ShowIpScript"

    private var actuator: IActuator

    override var listener: (String, Boolean, String) -> Unit = { command, result, message ->
        Log.d(tag, "$result : $message")
    }

    init {
        actuator = DefaultActuator { command, result, message ->
            listener.invoke(command, result, message)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            actuator.addCommand("/system/bin/sh")
        }else{
            actuator.addCommand("su")
        }
        actuator.addCommand("ifconfig wlan0")

    }

    override fun start() {
        GlobalScope.launch {
            actuator.execCommand()
        }
    }

    override fun stop() {

    }
}