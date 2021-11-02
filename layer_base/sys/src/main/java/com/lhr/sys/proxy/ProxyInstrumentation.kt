package com.lhr.sys.proxy

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

/**
 * @author lhr
 * @date 2021/10/8
 * @des
 */
class ProxyInstrumentation(private val baseIns: Instrumentation) : Instrumentation(){
    private val PI_TAG = "PILog"

    @SuppressLint("DiscouragedPrivateApi")
    fun execStartActivity(
        who: Context?, contextThread: IBinder?, token: IBinder?, target: Activity?,
        intent: Intent?, requestCode: Int, options: Bundle?
    ): ActivityResult? {
        Log.e(PI_TAG,
            "Context: " + who + " , IBinder: " + contextThread + " , IBinder: " + token + " , Activity: " + target + " " +
                    "Intent: " + intent + " , Int: " + requestCode + " , Bundle: " + options + "")
        return try {
            val realExec =
                Instrumentation::class.java.getDeclaredMethod(
                    "execStartActivity",
                    Context::class.java,
                    IBinder::class.java,
                    IBinder::class.java,
                    Activity::class.java,
                    Intent::class.java,
                    Int::class.javaPrimitiveType,
                    Bundle::class.java
                )
            realExec.isAccessible = true
            realExec.invoke(
                baseIns,
                who,
                contextThread,
                token,
                target,
                intent,
                requestCode,
                options
            ) as ActivityResult?
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}