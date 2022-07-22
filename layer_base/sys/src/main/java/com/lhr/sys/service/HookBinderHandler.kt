package com.lhr.sys.service

import android.os.IBinder
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @CreateDate: 2022/7/22
 * @Author: mac
 * @Description: Binder
 */
class HookBinderHandler(
    private val proxyObj: IBinder,
    private val bean: ServiceHookBean
) : IUniversalHandler(proxyObj) {
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
        if (method.name.equals("queryLocalInterface")) {
            toLog(this::class.java.simpleName, proxyObj, method, args)
            //这里直接返回真正被hook掉的Service接口
            //这个代理类必须实现IInterface接口
            return Proxy.newProxyInstance(
                proxyObj.javaClass.classLoader,
                arrayOf(bean.iinterface),
                HookBinderInvocationHandler(proxyObj, bean.stub)
            )
        }
        return super.invoke(proxy, method, args)
    }
}