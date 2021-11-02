//
// Created by liaohaorui on 2021/10/12.
//
#include <stdio.h>
#include <jni.h>
#include <android/log.h>

#define const char* LOG_TAG = "chaos-image";
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,__VA_ARGS__)

void sayHello(JNIEnv *env, jobject jobj){
    const char* mchars = "hello world\n";
    LOGE("%s", mchars);
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv *env = NULL;
    (*vm)->GetEnv(vm,(void**)&env,JNI_VERSION_1_6);
    //获取类对象
    jclass jclazz = (*env)->FindClass(env,"com/lhr/image/WallpaperActivity");

    //生成方法映射结构体
    JNINativeMethod methods_Mains[] = { { "sayHello", "()V", (void*)sayHello } };
    (*env)->RegisterNatives(env, jclazz, methods_Mains, sizeof(methods_Mains) / sizeof(methods_Mains[0]));
    return JNI_VERSION_1_6;
}

