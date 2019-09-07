#include <malloc.h>
#include "native-lib.h"
#include "x265.h"

// 设置全局的编码
x265_param *pParam = NULL;
x265_encoder *pEncoder = NULL;
int y_size;
x265_picture *pPictur_in = NULL;

JNIEXPORT jstring JNICALL
Java_com_example_libx265encode_H265Encode_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT void JNICALL
Java_com_example_libx265encode_H265Encode_initX265Encode(JNIEnv *env, jobject instance, jint width,
                                                         jint height, jint fps, jint bite) {
    pParam = x265_param_alloc();
    x265_param_default(pParam);
    pParam->bRepeatHeaders = 1; // write sps,pps before keyframe
    pParam->internalCsp = X265_CSP_I420; // 视频为420p
    pParam->sourceWidth = width;
    pParam->sourceHeight = height;
    pParam->fpsNum = fps; // 帧率分子
    pParam->fpsDenom = 1; // 帧率分母

    pEncoder = x265_encoder_open(pParam);
    if (pEncoder == NULL) {
        // 打开编码器失败
        return;
    }
    y_size = pParam->sourceWidth * pParam->sourceHeight;
    pPictur_in = x265_picture_alloc();
    x265_picture_init(pParam, pPictur_in);
    char *buff= (char *)malloc(y_size*3/2);
    pPictur_in->planes[0]=buff;
    pPictur_in->planes[1]=buff+y_size;
    pPictur_in->planes[2]=buff+y_size*5/4;
    pPictur_in->stride[0]=width;
    pPictur_in->stride[1]=width/2;
    pPictur_in->stride[2]=width/2;
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_libx265encode_H265Encode_encodeH265(JNIEnv *env, jobject instance,
                                                     jbyteArray inBuffer_, jint length) {
    jbyte *inBuffer = env->GetByteArrayElements(inBuffer_, NULL);

    x265_nal *pNals = NULL;
    uint32_t iNal = 0;
    int ret = x265_encoder_encode(pEncoder, &pNals, &iNal, pPictur_in, NULL);
    if (ret == 1) {
        // ret = 1 表示得到的编码的数据,ret = 0表示被缓存或者为空
        int bufsize = 0;
        for (int i=0; i< iNal; i++) {
            bufsize += pNals[i].sizeBytes;
        }

        char *tempdata = new char[bufsize];
        memset(tempdata, 0, bufsize);

        for (int i=0; i<iNal; i++) {
            if (pNals[i].payload != NULL) {
                memcpy(tempdata, pNals[i].payload, pNals[i].sizeBytes);
                tempdata += pNals[i].sizeBytes;
            }
        }
        jbyteArray byteArrayResult = env->NewByteArray(bufsize);
        env->SetByteArrayRegion(byteArrayResult,0,bufsize,(jbyte*)tempdata);
    }

    env->ReleaseByteArrayElements(inBuffer_, inBuffer, 0);
}