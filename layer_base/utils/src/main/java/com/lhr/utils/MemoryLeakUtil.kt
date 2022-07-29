package com.lhr.utils

import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author lhr
 * @date 2021/8/6
 * @des 内存泄漏优化工具
 */

/**
 * 将回调接口包装为内存泄漏优化的回调接口
 * @param T 需要被代理的接口
 * @param lifecycle 生命周期监听对象
 */
inline fun <reified T> Any.live(lifecycle: Lifecycle): T {
    if (this is T) {
        val wrapper = ProxyCallback(this)
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    wrapper.onDestroy()
                }
            }
        })
        return Proxy.newProxyInstance(this::class.java.classLoader, arrayOf(T::class.java), wrapper) as T
    } else {
        throw RuntimeException("${this::class.java} not implement interface ${T::class.java}")
    }
}

/**
 * 将回调接口包装为内存泄漏优化的回调接口
 * @param T 需要被代理的接口
 * @param view 生命周期监听对象
 */
inline fun <reified T> Any.live(view: View): T {
    if (this is T) {
        val wrapper = ProxyCallback(this)
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener{
            override fun onViewDetachedFromWindow(v: View?) {
                wrapper.onDestroy()
            }
            override fun onViewAttachedToWindow(v: View?) {
            }
        })
        return Proxy.newProxyInstance(this::class.java.classLoader, arrayOf(T::class.java), wrapper) as T
    } else {
        throw RuntimeException("${this::class.java} not implement interface ${T::class.java}")
    }
}

class ProxyCallback(private var concrete: Any?) : InvocationHandler {
    fun onDestroy(){
        this@ProxyCallback.concrete = null
    }

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        concrete?.let {
            Log.d("Proxy", this::class.java.simpleName + " >>> " + it::class.java.simpleName + " " + method.name)
            return method.invoke(concrete, *(args ?: arrayOfNulls<Any>(0)))
        }
        /**
         * 返回默认值
         */
        return when(method.returnType) {
            java.lang.Double.TYPE -> 0.0
            java.lang.Float.TYPE -> 0f
            java.lang.Character.TYPE -> '\u0000'
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.String::class.java -> ""
            java.lang.Void.TYPE -> null
            else -> null
        }
    }
}