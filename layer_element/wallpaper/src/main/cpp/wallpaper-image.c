//
// Created by liaohaorui on 2021/10/12.
//
#include <stdio.h>
#include <jni.h>

jstring _getNativeString(JNIEnv *env, jobject jobj){
    const char* mchars = "hello world\n";
    jstring message = (*env)->NewStringUTF(env,mchars);
    return message;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv *env = NULL;
    (*vm)->GetEnv(vm,(void**)&env,JNI_VERSION_1_6);
    //获取类对象
    jclass jclazz = (*env)->FindClass(env,"com/lhr/wallpaper/WallpaperActivity");

    //生成方法映射结构体
    JNINativeMethod methods_Mains[] = { { "getNativeString", "()Ljava/lang/String;", (void*)_getNativeString } };
    (*env)->RegisterNatives(env, jclazz, methods_Mains, sizeof(methods_Mains) / sizeof(methods_Mains[0]));
    return JNI_VERSION_1_6;
}

