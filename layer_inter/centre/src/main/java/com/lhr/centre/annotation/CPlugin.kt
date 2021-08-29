package com.lhr.centre.annotation



@kotlin.annotation.Target(AnnotationTarget.ANNOTATION_CLASS)
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
annotation class CPlugin(val name: String,val level: Int)
