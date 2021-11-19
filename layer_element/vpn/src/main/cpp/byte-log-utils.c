//
// Created by liaohaorui on 2021/11/17.
//

#include <stdlib.h>
#include <memory.h>
#include <math.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>

#ifndef _Included_com_lhr_vpn_util_ByteLog
#define _Included_com_lhr_vpn_util_ByteLog
#define  TAG_E(...) __android_log_print(ANDROID_LOG_ERROR, "NativeLog", __VA_ARGS__)

char* numToBinary(char c){
    char* bin = (char*)malloc(8 * sizeof(char) + 1);
    bin[8 * sizeof(char)] = '\0';
    u_char uc = c;
    u_char sign = 0x80;

    int i = 0;
    for (; i < 8; i++){
        if (uc & sign){
            bin[i] = '1';
        }
        else{
            bin[i] = '0';
        }
        uc = uc << 1;
    }
    return bin;
}

jstring charTojstring(JNIEnv *env, const char* str){
    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>","([BLjava/lang/String;)V");
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(str));
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(str), (jbyte *) str);
    jstring encoding = (*env)->NewStringUTF(env, "utf-8");
    return (jstring) (*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

JNIEXPORT jstring JNICALL Java_com_lhr_vpn_util_ByteLog_nativeGetByteBufferString
        (JNIEnv *env, jobject jobj, jbyteArray jba, jint jstart, jint jend) {
    int len = jend - jstart;
    jbyte *arrays = (jbyte *) malloc(len * sizeof(jbyte));
    char* chars = malloc(len * 8 * sizeof(char) + len + 1);
    chars[len * 8 * sizeof(char)] = '\0';
    (*env)->GetByteArrayRegion(env, jba, jstart, jend, arrays);
    int byteIndex = 0;
    int charsIndex = 0;
    for (; byteIndex < len; ++byteIndex) {
        u_char uc = arrays[byteIndex];
        u_char sign = 0x80;
        int z = 0;
        for (; z < 8; z++){
            if (uc & sign){
                chars[charsIndex++] = '1';
            }
            else{
                chars[charsIndex++] = '0';
            }
            uc = uc << 1;
        }
        chars[charsIndex++] = ',';
    }

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