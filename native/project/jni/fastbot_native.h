/*
 * Copyright (c) 2020 Bytedance Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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