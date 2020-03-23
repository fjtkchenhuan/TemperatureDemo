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

#include "AMG88XX_API.h"

#define  LOG_TAG  "amg88xx"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define DEVICE_NAME "/dev/amg88xx"

struct AMG88XX_W_DATA write_data;
struct AMG88XX_R_DATA read_data;

int amg88xx_fd = -1;  
extern float pixels[];

#ifndef _Included_com_ys_amg88xx_AMG88XX
#define _Included_com_ys_amg88xx_AMG88XX
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_ys_amg88xx_Amg88xx_open
  (JNIEnv * env, jclass cls,int refreshRate)
{
	uint16_t i = 0;
	uint16_t rate;
    if(amg88xx_fd > 0)
		return amg88xx_fd;

	amg88xx_fd = open(DEVICE_NAME, O_RDWR);  
	if(amg88xx_fd > 0){
		globe_write_data = &write_data;
		globe_read_data = &read_data;
		if(1 == refreshRate)
			rate = 1;
		else
			rate = 10;
		amg88xx_initialise(rate);
		return amg88xx_fd;
	}
	else{
        	printf("Failed to open device %s\n", DEVICE_NAME);
        	return -1;
	}
	
}

JNIEXPORT void JNICALL Java_com_ys_amg88xx_Amg88xx_close
  (JNIEnv * env, jclass cls)
{
    if (amg88xx_fd > 0){
		   fflush(stdout);
           close(amg88xx_fd);
           amg88xx_fd = -1;
    }
    return;
}

JNIEXPORT jfloatArray JNICALL Java_com_ys_amg88xx_Amg88xx_readTemperature
  (JNIEnv * env, jclass cls)
{
	jfloatArray floatArr = (*env)->NewFloatArray(env,68);
  	if (amg88xx_fd <= 0)
  		return NULL;
	else{
		amg88xx_measure();
		(*env)->SetFloatArrayRegion(env,floatArr,0,68,pixels);
	}
	return floatArr;

}

#ifdef __cplusplus
}
#endif
#endif
