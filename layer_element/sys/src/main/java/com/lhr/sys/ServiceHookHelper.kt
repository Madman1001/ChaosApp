package com.lhr.sys

import android.os.IBinder
import com.lhr.sys.service.HookBinderHandler
import com.lhr.sys.service.ServiceHookBean
import java.lang.reflect.Proxy


/**
 * @CreateDate: 2022/7/21
 * @Author: mac
 * @Description: 系统服务hook工具
 */
internal object ServiceHookHelper {
    internal fun hookService(bean: ServiceHookBean): Boolean{
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        val getServiceMethod = serviceManagerClass.getDeclaredMethod("getService", String::class.java)
        //获取被代理的Binder
        val rawBinder = getServiceMethod.invoke(null, bean.serviceName) as IBinder?
        rawBinder ?: return false

        //生成代理Binder
        val hookedBinder = Proxy.newProxyInstance(
                serviceManagerClass.classLoader,
                arrayOf(IBinder::class.java),
                HookBinderHandler(rawBinder, bean)
        ) as IBinder
        //替换代理类
        val cacheField = serviceManagerClass.getDeclaredField("sCache")
        cacheField.isAccessible = true
        val cache = cacheField.get(null) as MutableMap<String,IBinder>
        cache[bean.serviceName] = hookedBinder

        bean.extAction.invoke(hookedBinder)
        return true
    }
}