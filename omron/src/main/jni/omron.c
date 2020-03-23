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

#include "OMROM_API.h"

#define  LOG_TAG  "omron"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define DEVICE_NAME "/dev/omron"

struct omron_w_data write_data;
struct omron_r_data read_data;
int omron_fd = -1;  

extern int temperature[];

#ifndef _Included_com_ys_omron_OMROM
#define _Included_com_ys_omron_OMROM
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_ys_omron_Omron_open
  (JNIEnv * env, jclass cls,int type)
{
	uint16_t i = 0;
	uint16_t rate;
    if(omron_fd > 0)
		return omron_fd;

	omron_fd = open(DEVICE_NAME, O_RDWR);  
	if(omron_fd > 0){
		globe_write_data = &write_data;
		globe_read_data = &read_data;

		omron_initialise(type);
		return omron_fd;
	}
	else{
        	printf("Failed to open device %s\n", DEVICE_NAME);
        	return -1;
	}
	
}

JNIEXPORT void JNICALL Java_com_ys_omron_Omron_close
  (JNIEnv * env, jclass cls)
{
    if (omron_fd > 0){
		   fflush(stdout);
           close(omron_fd);
           omron_fd = -1;
    }
    return;
}

JNIEXPORT jintArray JNICALL Java_com_ys_omron_Omron_readTemperature1A
  (JNIEnv * env, jclass cls)
{
	jintArray intArr = (*env)->NewIntArray(env,2);
  	if (omron_fd <= 0)
  		return NULL;
	else{
		omron_measure(OMRON_1A_SENSOR);
		(*env)->SetIntArrayRegion(env,intArr,0,2,temperature);//To Ta
	}
	return intArr;
}

JNIEXPORT jintArray JNICALL Java_com_ys_omron_Omron_readTemperature8L
  (JNIEnv * env, jclass cls)
{
	jintArray intArr = (*env)->NewIntArray(env,11);
  	if (omron_fd <= 0)
  		return NULL;
	else{
		omron_measure(OMRON_8L_SENSOR);
		(*env)->SetIntArrayRegion(env,intArr,0,11,temperature);
	}
	return intArr;
}

JNIEXPORT jintArray JNICALL Java_com_ys_omron_Omron_readTemperature44L
  (JNIEnv * env, jclass cls)
{
	jintArray intArr = (*env)->NewIntArray(env,19);
  	if (omron_fd <= 0)
  		return NULL;
	else{
		omron_measure(OMRON_44L_SENSOR);
		(*env)->SetIntArrayRegion(env,intArr,0,19,temperature);
	}
	return intArr;
}

JNIEXPORT jintArray JNICALL Java_com_ys_omron_Omron_readTemperature32L
  (JNIEnv * env, jclass cls)
{
	jintArray intArr = (*env)->NewIntArray(env,1027);
  	if (omron_fd <= 0)
  		return NULL;
	else{
		omron_measure(OMRON_32L_SENSOR);
		(*env)->SetIntArrayRegion(env,intArr,0,1027,temperature);
	}
	return intArr;
}
#ifdef __cplusplus
}
#endif
#endif
