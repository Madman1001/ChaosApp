package com.lhr.common.ext

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

/**
 * @CreateDate: 2022/11/17
 * @Author: mac
 * @Description:
 */
//一个全局的ViewModel
@MainThread
public inline fun <reified VM : ViewModel> ComponentActivity.applicationViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        defaultViewModelProviderFactory
    }
    return ViewModelLazy(VM::class, {applicationViewModelStore }, factoryPromise)
}

val applicationViewModelStore: ViewModelStore by lazy {
    ViewModelStore()
}