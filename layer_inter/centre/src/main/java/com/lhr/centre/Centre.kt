package com.lhr.centre

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.lhr.centre.element.ElementData
import com.lhr.centre.element.IElementTable
import com.lhr.centre.element.TableConstant
import com.lhr.centre.utils.DexUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.lang.Exception

object Centre {
    private val tag = Centre::class.java.simpleName

    @Volatile
    var isInit = false
        private set

    private lateinit var tables: List<ElementData>

    private var initCallback: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())

    @Synchronized
    fun initCentre(context: Context) {
        if (!isInit) {
            GlobalScope.launch(Dispatchers.IO) {
                val start = System.currentTimeMillis()
                tables = getAllElement(context)
                Log.d(tag, "init over consume ${System.currentTimeMillis() - start}ms")
                isInit = true
                handler.post {
                    initCallback?.invoke()
                    initCallback = null
                }
            }
        }
    }

    fun getElementList(): List<ElementData> {
        if (isInit) {
            return tables
        }
        return listOf()
    }

    fun setInitCallback(callback: () -> Unit) {
        this.initCallback = callback
    }

    private fun getAllElement(context: Context): List<ElementData>{
        val listPlugin = ArrayList<ElementData>()
        for (clazzName in DexUtil.getDexAllClassName(context)) {
            if (clazzName.contains(Regex("${TableConstant.DEFAULT_PACKAGE}.*"))) {
                try {
                    val clazz = Class.forName(clazzName)
                    if (clazz.interfaces.contains(IElementTable::class.java)) {
                        listPlugin.add(generateElementData(clazz.newInstance() as IElementTable))
                    }
                    Log.d(tag, clazzName)
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return listPlugin
    }

    private fun generateElementData(table: IElementTable): ElementData {
        val extraMap = HashMap<String, String>()
        val array = JSONArray(table.getExtra())
        for (i in 0 until array.length()) {
            val keyValue = array.getJSONObject(i)
            keyValue.keys().forEach {
                extraMap[it] = keyValue.getString(it)
            }
        }
        return ElementData(
            table.getTitle() ?: "NULL",
            extraMap
        )
    }
}