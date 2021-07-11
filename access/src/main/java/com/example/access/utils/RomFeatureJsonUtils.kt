package com.example.access.utils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.example.access.bean.RomFeatureBean
import org.json.JSONObject
import java.io.InputStream

object RomFeatureJsonUtils {
    private const val FEATURE_ASSET_FILE = "rom_feature_config.json"
    private const val ROM_FEATURE_PRIMARY_KEY = "rom_items"

    /**
     * rom feature标签
     */
    private const val FEATURE_ID_KEY = "rom_id"
    private const val FEATURE_NAME_KEY = "rom_name"
    private const val FEATURE_FEATURE_ITEMS_KEY = "feature_items"

    /**
     * feature item 标签
     */
    private const val ITEM_KEY_KEY = "key"
    private const val ITEM_VALUE_KEY = "value"
    private const val ITEM_CONDITION_KEY = "condition"

    private val romFeatures = ArrayList<RomFeatureBean>()
    private var isInit = false

    fun getRomCode(context: Context): Int{
        if (!isInit){
            init(context)
            isInit = true
        }

        for (bean in romFeatures) {
            var result = true
            for (item in bean.features){
                result = result && RomMatchUtils.matchFeatureBean(item)
                if (!result){
                    break
                }
            }
            if (result){
                return bean.code
            }
        }
        return 0
    }

    private fun init(context: Context){
        try {
            var inputStream: InputStream? =openAccessFile(context)
            if (inputStream != null) {
                val jsonData: String? = IoUtils.stream2String(inputStream)
                if (jsonData != null) {
                    readFeatureConfig(JSONObject(jsonData))
                }
                try {
                    inputStream.close()
                } catch (e2: java.lang.Exception) {
                    e2.printStackTrace()
                }
            }else{
                try {
                    inputStream?.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e3: java.lang.Exception) {
            e3.printStackTrace()
        }
    }

    /**
     * 读取json文件生成RomFeatureBean对象
     */
    private fun readFeatureConfig(jsonObject: JSONObject){
        if (jsonObject.has(ROM_FEATURE_PRIMARY_KEY)){
            val jsonArray = jsonObject.getJSONArray(ROM_FEATURE_PRIMARY_KEY)
            for (index in 0 until jsonArray.length()){

                val bean = createRomFeatureBean(jsonArray.getJSONObject(index))
                if (bean != null){
                    romFeatures.add(bean)
                }
            }
        }
    }

    private fun createRomFeatureBean(jsonObject: JSONObject): RomFeatureBean? {
        var bean: RomFeatureBean? = null
        if (jsonObject.has(FEATURE_ID_KEY)){
            bean = RomFeatureBean(jsonObject.getInt(FEATURE_ID_KEY))
        }

        if (jsonObject.has(FEATURE_NAME_KEY)){
            bean?.desc = jsonObject.getString(FEATURE_NAME_KEY)
        }

        if (jsonObject.has(FEATURE_FEATURE_ITEMS_KEY)){
            val jsonArray = jsonObject.getJSONArray(FEATURE_FEATURE_ITEMS_KEY)
            for (index in 0 until jsonArray.length()){
                val item = createFeatureItemBean(jsonArray.getJSONObject(index))
                if (item != null){
                    bean?.features?.add(item)
                }
            }
        }
        return bean
    }

    private fun createFeatureItemBean(jsonObject: JSONObject): RomFeatureBean.FeatureBean? {
        if (jsonObject.has(ITEM_KEY_KEY)
            && jsonObject.has(ITEM_VALUE_KEY)
            && jsonObject.has(ITEM_CONDITION_KEY)){
                val key = jsonObject.getString(ITEM_KEY_KEY)
                val value = jsonObject.getString(ITEM_VALUE_KEY)
                val condition = jsonObject.getString(ITEM_CONDITION_KEY)
            return RomFeatureBean.FeatureBean(key, value, condition)
        }
        return null
    }

    /**
     * 打开json文件
     */
    private fun openAccessFile(context: Context): InputStream? {
        val assets: AssetManager = context.resources.assets
        return try {
            assets.open(FEATURE_ASSET_FILE)
        }catch (e2: Exception) {
            e2.printStackTrace()
            null
        }
    }
}