#include <jni.h>
#include <string>
#include <android/log.h>
#include "libmp3lame/lame.h"

#define QUALITY_HIGH 3
#define QUALITY_MIDDLE 5
#define QUALITY_LOW 7

#define TAG "LIB_LAME"

#define LOGD(fmt, ...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, __VA_ARGS__)


extern "C"
JNIEXPORT long JNICALL
Java_com_github_zy3274311_libmp3lame_LameEncoder_init(JNIEnv *env, jobject thiz, jint sample_rate_in,
                                                      jint sample_rate_out, jint out_channels,
                                                      jint bitrate) {
    // TODO: implement init()
    lame_global_flags * gfp = lame_init();
    const char* fmt = "init lame_global_flags %d";
    LOGD(fmt, gfp);

    lame_set_in_samplerate(gfp, sample_rate_in);
    lame_set_num_channels(gfp, out_channels);
    lame_set_out_samplerate(gfp, sample_rate_out);
    lame_set_quality(gfp, QUALITY_HIGH);
    lame_set_brate(gfp, bitrate);
    lame_init_params(gfp);
    return reinterpret_cast<long>(gfp);
}

extern "C"
JNIEXPORT int JNICALL
Java_com_github_zy3274311_libmp3lame_LameEncoder_encode(JNIEnv *env, jobject thiz,jlong pr,
                                                        jshortArray left_pcm, jshortArray right_pcm,
                                                        jint size_in_shorts, jbyteArray mp3buffer) {
    // TODO: implement encode()
    auto * gfp = reinterpret_cast<lame_global_flags *>(pr);
    jsize size = env->GetArrayLength(mp3buffer);
    jshort *l = env->GetShortArrayElements(left_pcm, nullptr);
    jshort *r = env->GetShortArrayElements(right_pcm, nullptr);
    jbyte *mp3 = env->GetByteArrayElements(mp3buffer, nullptr);
    auto  *mp3buf = reinterpret_cast<unsigned char *>(mp3);

    int result = lame_encode_buffer(gfp, l, r, size_in_shorts, mp3buf, size);

    env->ReleaseShortArrayElements(left_pcm, l, 0);
    env->ReleaseShortArrayElements(right_pcm, r, 0);
    env->ReleaseByteArrayElements(mp3buffer, mp3, 0);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_github_zy3274311_libmp3lame_LameEncoder_flush(JNIEnv *env, jobject thiz, jlong pr,
                                                       jbyteArray mp3buffer) {
    // TODO: implement flush()
    auto * gfp = reinterpret_cast<lame_global_flags *>(pr);
    jsize size = env->GetArrayLength(mp3buffer);
    jbyte *mp3 = env->GetByteArrayElements(mp3buffer, nullptr);
    int result = lame_encode_flush(gfp, reinterpret_cast<unsigned char *>(mp3), size);
    env->ReleaseByteArrayElements(mp3buffer, mp3, 0);

    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_github_zy3274311_libmp3lame_LameEncoder_release(JNIEnv *env, jobject thiz, jlong pr) {
    // TODO: implement release()
    auto * gfp = reinterpret_cast<lame_global_flags *>(pr);
    const char* fmt = "release lame_global_flags %d";
    LOGD(fmt, gfp);
    lame_close(gfp);
}