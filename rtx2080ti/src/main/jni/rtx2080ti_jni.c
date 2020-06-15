/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/watchdog.h>
#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <string.h>

#include "rtx2080ti.h"

#define  LOG_TAG  "rtx2080ti"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

//#define DEVICE_NAME "/dev/mlx90621"

//struct MLX90621_W_DATA write_data;
//struct MLX90621_R_DATA read_data;
//int mlx90621_fd = -1;  
//extern float temperatures[];
extern int rtx2080ti_fd;

#ifndef _Included_com_ys_rtx2080ti_Rtx2080ti
#define _Included_com_ys_rtx2080ti_Rtx2080ti
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_ys_rtx2080ti_Rtx2080ti_open
  (JNIEnv * env, jclass cls,int refreshRate)
{
	uint16_t i = 0;
	uint16_t rate;
   	 if(rtx2080ti_fd > 0)
		return rtx2080ti_fd;

	int result = OpenModule(); 
	if(0 == result){
		SetModuleFramerate(refreshRate);
		return rtx2080ti_fd;
	}
	else{
        	printf("Failed to open device %s\n", DEVICE_NAME);
        	return -1;
	}
	
}

JNIEXPORT void JNICALL Java_com_ys_rtx2080ti_Rtx2080ti_close
  (JNIEnv * env, jclass cls)
{
	ReleaseModule();
    return;
}

JNIEXPORT jfloatArray JNICALL Java_com_ys_rtx2080ti_Rtx2080ti_readTemperature
  (JNIEnv * env, jclass cls)
{
	jfloatArray floatArr = (*env)->NewFloatArray(env,257);
  	if (rtx2080ti_fd <= 0)
  		return NULL;
	else{
		Rtx2080ti_readoutdata tmp={0};
		ReadOutModuleData(&tmp);
		ParseTemperature(&tmp); //如果需要原始数据需要去掉此处并且把buffer扩大到525字节	
		(*env)->SetFloatArrayRegion(env,floatArr,0,257,&temp);
	}
	return floatArr;

}

#ifdef __cplusplus
}
#endif
#endif
