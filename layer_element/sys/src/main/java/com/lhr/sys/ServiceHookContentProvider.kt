package com.lhr.sys

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.util.Log
import com.lhr.sys.reflection.HiddenApiBypass
import java.lang.reflect.InvocationTargetException


class ServiceHookContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val application = context?.applicationContext ?: getApplicationByReflect()
        SERVICE_HOOK_LIST.forEach {
            if (!ServiceHookHelper.hookService(it)) {
                HookFailList.add(it)
                Log.e(HOOK_TAG, "service ${it.serviceName} hook fail")
            } else {
                Log.e(HOOK_TAG, "service ${it.serviceName} hook success")
            }
        }
        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    fun getApplicationByReflect(): Application {
        try {
            @SuppressLint("PrivateApi") val activityThread =
                Class.forName("android.app.ActivityThread")
            val thread = activityThread.getMethod("currentActivityThread").invoke(null)
            val app = activityThread.getMethod("getApplication").invoke(thread)
                ?: throw NullPointerException("you should init first")
            return app as Application
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        throw NullPointerException("you should init first")
    }
}