/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */
/**
 * @authors Jianqiang Guo, Yuhui Su
 */
#include <jni.h>

#ifndef __Fastbot_Native_Jni_H__
#define __Fastbot_Native_Jni_H__
#ifdef __cplusplus
extern "C" {
#endif

// getAction
JNIEXPORT jstring JNICALL
Java_com_bytedance_fastbot_AiClient_b0bhkadf(JNIEnv *env, jobject, jstring, jstring);

//InitAgent
JNIEXPORT void JNICALL
Java_com_bytedance_fastbot_AiClient_fgdsaf5d(JNIEnv *env, jobject, jint, jstring, jint);

//loadResMapping
JNIEXPORT void JNICALL Java_com_bytedance_fastbot_AiClient_jdasdbil(JNIEnv *env, jobject, jstring);

JNIEXPORT jboolean JNICALL
Java_com_bytedance_fastbot_AiClient_nkksdhdk(JNIEnv *env, jobject, jstring activity, jfloat pointX,
                                             jfloat pointY);
JNIEXPORT jstring JNICALL
Java_com_bytedance_fastbot_AiClient_getNativeVersion(JNIEnv *env, jclass clazz);

#ifdef __cplusplus
}
#endif

#endif //__Fastbot_Native_Jni_H__