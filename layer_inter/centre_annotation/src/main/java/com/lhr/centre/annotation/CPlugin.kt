package com.lhr.centre.annotation

@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
annotation class CPlugin(val name: String,val level: Int)
