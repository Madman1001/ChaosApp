package com.lhr.common.ext

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import java.lang.reflect.ParameterizedType

/**
 * @CreateDate: 2022/8/18
 * @Author: mac
 * @Description: ViewBinding 功能扩展
 */
fun <VB : ViewDataBinding> LifecycleOwner.createViewBinding(dbClass: Class<VB>, layoutInflater: LayoutInflater, parent: ViewGroup? = null, attachToParent: Boolean = false): VB {
    val viewDataBinding = dbClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        .invoke(null, layoutInflater, parent, attachToParent) as VB
    viewDataBinding.also {
        viewDataBinding.lifecycleOwner = this
    }
    return viewDataBinding
}

fun obtainBindingClassByParadigm(any: Any): Class<ViewDataBinding> {
    var superclass = any.javaClass.superclass
    var genericSuperclass = any.javaClass.genericSuperclass
    while (superclass != null){
        if (genericSuperclass is ParameterizedType){
            for (argument in genericSuperclass.actualTypeArguments) {
                runCatching {
                    if ((argument as Class<*>).superclass.equals(ViewDataBinding::class.java)){
                        return argument as Class<ViewDataBinding>
                    }
                }
            }
        }
        genericSuperclass = superclass.genericSuperclass
        superclass = superclass.superclass
    }
    throw IllegalArgumentException("There is no generic of ViewBinding.")
}
