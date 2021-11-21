package com.lhr.adb.script

import android.util.Log
import com.lhr.adb.exce.DefaultActuator
import com.lhr.adb.exce.IActuator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author lhr
 * @date 2021/11/21
 * @des 开启adb调试脚本
 */
class OpenAdbScript : IScript {
    private val tag = "OpenAdbScript"

    private var actuator: IActuator

    var listener: (String, Boolean, String) -> Unit = { command, result, message ->
        Log.d(tag, "$result : $message")
    }

    init {
        actuator = DefaultActuator { command, result, message ->
            listener.invoke(command, result, message)
        }
        actuator.addCommand("su")
        actuator.addCommand("setprop service.adb.tcp.port 5555")
        actuator.addCommand("stop adbd")
        actuator.addCommand("start adbd")
    }

    override fun start() {
        GlobalScope.launch {
            actuator.exceCommand()
        }
    }

    override fun stop() {

    }
}