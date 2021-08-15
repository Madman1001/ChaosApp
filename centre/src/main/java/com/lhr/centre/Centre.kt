package com.lhr.centre

import android.content.Context
import android.util.Log
import com.lhr.centre.router.IPlugin
import com.lhr.centre.utils.DexUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

object Centre {
    private val tag = Centre::class.java.simpleName

    private var isInit = false
    private lateinit var plugins: Array<IPlugin>

    fun initCentre(context: Context){
        if (!isInit){
            GlobalScope.launch(Dispatchers.IO) {
                val start = System.currentTimeMillis()
                val listPlugin = ArrayList<IPlugin>()
                for (clazzName in DexUtil.getDexAllClassName(context)) {
                    try {
                        val clazz = Class.forName(clazzName)
                        if (clazz.interfaces.contains(IPlugin::class.java)){
                            listPlugin.add(clazz.newInstance() as IPlugin)
                        }
                        Log.e(tag,clazzName)
                    }catch (e:Exception){
                        continue
                    }
                }
                plugins = listPlugin.toTypedArray()
                Log.e(tag,"init over consume ${System.currentTimeMillis() - start}ms")
                Log.e(tag,"$plugins")
                isInit = true
            }
        }
    }
}