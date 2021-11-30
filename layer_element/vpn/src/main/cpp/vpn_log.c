//
// Created by liaohaorui on 2021/11/17.
//
#ifndef _Included_com_lhr_vpn_util_ByteLog
#define _Included_com_lhr_vpn_util_ByteLog

#include <stdlib.h>
#include <memory.h>
#include <math.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include "vpn_java_utils.c"

#define  TAG_E(...) __android_log_print(ANDROID_LOG_ERROR, "NativeLog", __VA_ARGS__)

JNIEXPORT jstring JNICALL Java_com_lhr_vpn_util_ByteLog_nativeGetByteBufferString
        (JNIEnv *env, jobject jobj, jbyteArray jba) {
    int len = (*env)->GetArrayLength(env,jba);
    jbyte *arrays = (jbyte *) malloc(len * sizeof(jbyte));
    unsigned char* chars = malloc(len * 8 * sizeof(unsigned char) + len + 1);
    (*env)->GetByteArrayRegion(env, jba, 0, len, arrays);
    int byteIndex = 0;
    int charsIndex = 0;
    for (; byteIndex < len; ++byteIndex) {
        unsigned char uc = (unsigned char)arrays[byteIndex];
        unsigned char sign = 0x80;
        int z = 0;
        for (; z < 8; z++){
            if (uc & sign){
                chars[charsIndex++] = '1';
            }
            else{
                chars[charsIndex++] = '0';
            }
            sign = sign >> 1;
        }
        chars[charsIndex++] = ',';
    }
    chars[charsIndex - 1] = '\0';

    jstring message = charTojstring(env,chars);

    //检查是否有异常
    jboolean has_exception =  (*env)->ExceptionCheck(env);
    if (has_exception) {
        (*env)->ExceptionDescribe(env);
        //清空异常
        (*env)->ExceptionClear(env);
        (*env)->ThrowNew(env,(*env)->FindClass(env,"java/lang/Exception"),"创建字符串出现异常");
        return NULL;
    }

    free(chars);

    free(arrays);

    return message;
}

#endif