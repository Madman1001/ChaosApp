#ifndef _Included_vpn_java_utils
#define _Included_vpn_java_utils

#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <memory.h>

static jstring charTojstring(JNIEnv *env, const char* str){
    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>","([BLjava/lang/String;)V");
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(str));
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(str), (jbyte *) str);
    jstring encoding = (*env)->NewStringUTF(env, "utf-8");
    return (jstring) (*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

static char * jstringToChar(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env, "utf-8");
    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) (*env)->CallObjectMethod(env, jstr, mid, strencode);
    jsize alen = (*env)->GetArrayLength(env, barr);
    jbyte *ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    (*env)->ReleaseByteArrayElements(env, barr, ba, 0);
    return rtn;
}

static jobject int2Integer(JNIEnv *env, int val){
    jclass intClass = (*env)->FindClass(env, "java/lang/Integer");
    jmethodID ctorId = (*env)->GetMethodID(env, intClass, "<init>", "(I)V");
    return (*env)->NewObject(env, intClass, ctorId, (jint) val);
}

static int Integer2int(JNIEnv *env, jobject jobj){
    jclass dataClass = (*env)->FindClass(env, "java/lang/Integer");
    if ((*env)->IsInstanceOf(env, jobj, dataClass) == JNI_TRUE) {
        jmethodID mid = (*env)->GetMethodID(env, dataClass, "intValue", "()I");
        return ((*env)->CallIntMethod(env, jobj,mid));
    } else {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "Integer转int出现异常");
        return 0;
    }
}

static jobject char2Byte(JNIEnv *env, char val){
    jclass intClass = (*env)->FindClass(env, "java/lang/Byte");
    jmethodID ctorId = (*env)->GetMethodID(env, intClass, "<init>", "(B)V");
    return (*env)->NewObject(env, intClass, ctorId, (jint) val);
}

static jchar Byte2char(JNIEnv *env, jobject jobj){
    jclass dataClass = (*env)->FindClass(env, "java/lang/Byte");
    if ((*env)->IsInstanceOf(env, jobj, dataClass) == JNI_TRUE) {
        jmethodID mid = (*env)->GetMethodID(env, dataClass, "byteValue", "()B");
        return  ((*env)->CallByteMethod(env, jobj, mid));
    } else {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "Byte转char出现异常");
        return 0;
    }
}

static jobject short2Short(JNIEnv *env, short val){
    jclass intClass = (*env)->FindClass(env, "java/lang/Short");
    jmethodID ctorId = (*env)->GetMethodID(env, intClass, "<init>", "(S)V");
    return (*env)->NewObject(env, intClass, ctorId, (jint) val);
}

static int Short2short(JNIEnv *env, jobject jobj){
    jclass dataClass = (*env)->FindClass(env, "java/lang/Short");
    if ((*env)->IsInstanceOf(env, jobj, dataClass) == JNI_TRUE) {
        jmethodID mid = (*env)->GetMethodID(env, dataClass, "shortValue", "()S");
        return ((*env)->CallShortMethod(env, jobj,mid));
    } else {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "Short转short出现异常");
        return 0;
    }
}

static char* charToBinary(char c){
    char* bin = (char*)malloc(8 * sizeof(char) + 1);
    bin[8 * sizeof(char)] = '\0';
    unsigned char uc = c;
    unsigned char sign = 0x80;

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
#endif