//
// Created by liaohaorui on 2021/11/17.
//
#ifndef _Included_vpn_log
#define _Included_vpn_log
#include <jni.h>
#include <android/log.h>

#define  TAG_V(...) __android_log_print(ANDROID_LOG_VERBOSE, "NativeLog", __VA_ARGS__)
#define  TAG_D(...) __android_log_print(ANDROID_LOG_DEBUG, "NativeLog", __VA_ARGS__)
#define  TAG_I(...) __android_log_print(ANDROID_LOG_INFO, "NativeLog", __VA_ARGS__)
#define  TAG_W(...) __android_log_print(ANDROID_LOG_WARN, "NativeLog", __VA_ARGS__)
#define  TAG_E(...) __android_log_print(ANDROID_LOG_ERROR, "NativeLog", __VA_ARGS__)

#endif