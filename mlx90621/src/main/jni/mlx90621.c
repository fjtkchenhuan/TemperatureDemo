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

#include "MLX90621_API.h"

#define  LOG_TAG  "mlx90621"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define DEVICE_NAME "/dev/mlx90621"

struct MLX90621_W_DATA write_data;
struct MLX90621_R_DATA read_data;
int mlx90621_fd = -1;  
extern float temperatures[];


#ifndef _Included_com_ys_mlx90621_MLX90621
#define _Included_com_ys_mlx90621_MLX90621
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_ys_mlx90621_Mlx90621_open
  (JNIEnv * env, jclass cls,int refreshRate)
{
	uint16_t i = 0;
	uint16_t rate;
    if(mlx90621_fd > 0)
		return mlx90621_fd;

	mlx90621_fd = open(DEVICE_NAME, O_RDWR);  
	if(mlx90621_fd > 0){
		globe_write_data = &write_data;
		globe_read_data = &read_data;
		switch (refreshRate) {
			case 0:
				rate = 0;
				break;
			case 1:
				rate = 1;
				break;
			case 2:
				rate = 2;
				break;
			case 4:
				rate = 4;
				break;
			case 8:
				rate = 8;
				break;
			case 16:
				rate = 16;
				break;
			case 32:
				rate = 32;
				break;
			default:
				rate = 1;
		}		
		mlx90621_initialise(rate);
		return mlx90621_fd;
	}
	else{
        	printf("Failed to open device %s\n", DEVICE_NAME);
        	return -1;
	}
	
}

JNIEXPORT void JNICALL Java_com_ys_mlx90621_Mlx90621_close
  (JNIEnv * env, jclass cls)
{
    if (mlx90621_fd > 0){
		   fflush(stdout); 
           close(mlx90621_fd);
           mlx90621_fd = -1;
    }
    return;
}

JNIEXPORT jfloatArray JNICALL Java_com_ys_mlx90621_Mlx90621_readTemperature
  (JNIEnv * env, jclass cls)
{
	jfloatArray floatArr = (*env)->NewFloatArray(env,68);
  	if (mlx90621_fd <= 0)
  		return NULL;
	else{
		mlx90621_measure();	
		(*env)->SetFloatArrayRegion(env,floatArr,0,68,temperatures);
	}
	return floatArr;

}

#ifdef __cplusplus
}
#endif
#endif
