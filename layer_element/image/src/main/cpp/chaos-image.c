//
// Created by liaohaorui on 2021/10/12.
//
#include <stdlib.h>
#include <memory.h>
#include <math.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <sys/types.h>
#include <dirent.h>

#define const char* LOG_TAG = "chaos-image";
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,"%s", __VA_ARGS__)

/**
 * 高斯模糊算法
 * @param pix
 * @param w
 * @param h
 * @param radius
 */
void gaussBlur(int* pix, int w, int h, int radius)
{
    float sigma = (float) (1.0 * radius / 2.57);
    float deno  = (float) (1.0 / (sigma * sqrt(2.0 * M_PI)));
    float nume  = (float) (-1.0 / (2.0 * sigma * sigma));
    float* gaussMatrix = (float*)malloc(sizeof(float)* (radius + radius + 1));
    float gaussSum = 0.0;
    for (int i = 0, x = -radius; x <= radius; ++x, ++i)
    {
        float g = (float) (deno * exp(1.0 * nume * x * x));
        gaussMatrix[i] = g;
        gaussSum += g;
    }
    int len = radius + radius + 1;
    for (int i = 0; i < len; ++i)
        gaussMatrix[i] /= gaussSum;
    int* rowData  = (int*)malloc(w * sizeof(int));
    int* listData = (int*)malloc(h * sizeof(int));
    for (int y = 0; y < h; ++y)
    {
        memcpy(rowData, pix + y * w, sizeof(int) * w);
        for (int x = 0; x < w; ++x)
        {
            float r = 0, g = 0, b = 0;
            gaussSum = 0;
            for (int i = -radius; i <= radius; ++i)
            {
                int k = x + i;
                if (0 <= k && k <= w)
                {
                    //得到像素点的rgb值
                    int color = rowData[k];
                    int cr = (color & 0x00ff0000) >> 16;
                    int cg = (color & 0x0000ff00) >> 8;
                    int cb = (color & 0x000000ff);
                    r += cr * gaussMatrix[i + radius];
                    g += cg * gaussMatrix[i + radius];
                    b += cb * gaussMatrix[i + radius];
                    gaussSum += gaussMatrix[i + radius];
                }
            }
            int cr = (int)(r / gaussSum);
            int cg = (int)(g / gaussSum);
            int cb = (int)(b / gaussSum);
            pix[y * w + x] = cr << 16 | cg << 8 | cb | 0xff000000;
        }
    }
    for (int x = 0; x < w; ++x)
    {
        for (int y = 0; y < h; ++y)
            listData[y] = pix[y * w + x];
        for (int y = 0; y < h; ++y)
        {
            float r = 0, g = 0, b = 0;
            gaussSum = 0;
            for (int j = -radius; j <= radius; ++j)
            {
                int k = y + j;
                if (0 <= k && k <= h)
                {
                    int color = listData[k];
                    int cr = (color & 0x00ff0000) >> 16;
                    int cg = (color & 0x0000ff00) >> 8;
                    int cb = (color & 0x000000ff);
                    r += cr * gaussMatrix[j + radius];
                    g += cg * gaussMatrix[j + radius];
                    b += cb * gaussMatrix[j + radius];
                    gaussSum += gaussMatrix[j + radius];
                }
            }
            int cr = (int)(r / gaussSum);
            int cg = (int)(g / gaussSum);
            int cb = (int)(b / gaussSum);
            pix[y * w + x] = cr << 16 | cg << 8 | cb | 0xff000000;
        }
    }
    free(gaussMatrix);
    free(rowData);
    free(listData);
}

jobject _nativeBlurBitmap(JNIEnv *env, jobject jobj, jobject jbmp){
    const char* start_message = "start _nativeBlurBitmap\n";
    LOGE(start_message);

    AndroidBitmapInfo  info = {0};
    int *data = NULL;

    AndroidBitmap_getInfo(env,jbmp,&info);

    AndroidBitmap_lockPixels(env,jbmp, (void**) &data);

    gaussBlur(data,info.width,info.height,80);

    AndroidBitmap_unlockPixels(env,jbmp);

    char* end_message = "end _nativeBlurBitmap\n";
    LOGE(end_message);
    return jbmp;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv *env = NULL;
    (*vm)->GetEnv(vm,(void**)&env,JNI_VERSION_1_6);
    //获取类对象
    jclass jclazz = (*env)->FindClass(env,"com/lhr/image/ImageActivity");

    //生成方法映射结构体
    JNINativeMethod methods_Mains[] = { { "nativeBlurBitmap", "(Landroid/graphics/Bitmap;)Landroid/graphics/Bitmap;", (void*)_nativeBlurBitmap } };
    (*env)->RegisterNatives(env, jclazz, methods_Mains, sizeof(methods_Mains) / sizeof(methods_Mains[0]));

    return JNI_VERSION_1_6;
}

