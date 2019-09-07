//
// Created by xhp on 19-7-21.
//
#include <jni.h>
#include <string>
#ifndef LIBX265ENCODE_NATIVE_LIB_H
#define LIBX265ENCODE_NATIVE_LIB_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL
Java_com_example_libx265encode_H265Encode_stringFromJNI(JNIEnv *env,jobject /* this */);

JNIEXPORT void JNICALL
Java_com_example_libx265encode_H265Encode_initX265Encode(JNIEnv *env, jobject instance, jint width,
                                                         jint height, jint fps, jint bite);
JNIEXPORT jbyteArray JNICALL
Java_com_example_libx265encode_H265Encode_encodeH265(JNIEnv *env, jobject instance,
                                                     jbyteArray inBuffer_, jint length);
#ifdef __cplusplus
}
#endif

#endif //LIBX265ENCODE_NATIVE_LIB_H
