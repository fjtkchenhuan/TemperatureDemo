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

#include "MLX90641_API.h"

#define  LOG_TAG  "mlx90641"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define DEVICE_NAME "/dev/mlx90640"
#define  Rate2HZ   0x02
#define  Rate4HZ   0x03
#define  Rate8HZ   0x04
#define  Rate16HZ  0x05
#define  Rate32HZ  0x06

#define	 RefreshRate Rate8HZ 
#define  TA_SHIFT 5 //Default shift for MLX90641 in open air

struct w_SE_DATA write_data;
struct SE_DATA read_data;
int mlx90641_fd = -1;  
paramsMLX90641 mlx90641;
float mlx90641To_Sort[192];

#ifndef _Included_com_ys_temperaturei2c_MLX90641
#define _Included_com_ys_temperaturei2c_MLX90641
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_ys_mlx90641_Mlx90641_open
  (JNIEnv * env, jclass cls,int value)
{
	uint16_t i = 0;
	static uint16_t eeMLX90641[832]; 
	uint16_t frame[242];
	uint8_t rate = 0;
	globe_write_data = &write_data;
	globe_read_data = &read_data;
	mlx90641_fd = open(DEVICE_NAME, O_RDWR);  
	printf("mlx90641 open mlx90641_fd=%d\n",mlx90641_fd);  	
	if(mlx90641_fd > 0){
		if(0 == value)
			rate = Rate2HZ;
		else if(1 == value)
			rate = Rate4HZ;
		else if(2 == value)
			rate = Rate8HZ;
		else
			rate = Rate16HZ;		
		MLX90641_SetRefreshRate(mlx90641_fd,rate);
		MLX90641_DumpEE(mlx90641_fd, eeMLX90641);
		MLX90641_ExtractParameters(eeMLX90641, &mlx90641);	
		return mlx90641_fd;
	}
	else{
        	printf("Failed to open device %s\n", DEVICE_NAME);  
        	return -1;  		
	}	
}

JNIEXPORT void JNICALL Java_com_ys_mlx90641_Mlx90641_close
  (JNIEnv * env, jclass cls)
{
    if (mlx90641_fd > 0){
		   fflush(stdout); 
           close(mlx90641_fd);
           mlx90641_fd = -1;
    }
    return;
}

void bubbleSort(int len)
{
	int i,j;
	float temp;
    for (i = 0; i < len - 1; i++) {
        for (j = 0; j < len - 1 - i; j++) {
            if (mlx90641To_Sort[j] > mlx90641To_Sort[j+1]) {
                temp = mlx90641To_Sort[j+1];
                mlx90641To_Sort[j+1] = mlx90641To_Sort[j];
                mlx90641To_Sort[j] = temp;
            }
        }
    }
}

void MLX90641_Get_Temperature_Data(int fd,float *mlx90641To)
{
	uint16_t i=0,j=0;
	float Ta,tr;
	float emissivity=0.95;	
	uint16_t frame[242];
	
	MLX90641_GetFrameData(fd, frame);
	Ta = MLX90641_GetTa(frame, &mlx90641);		
	tr = Ta - TA_SHIFT;
	MLX90641_CalculateTo(frame, &mlx90641, emissivity, tr, mlx90641To);
	mlx90641To[192] = Ta;
}

JNIEXPORT jfloatArray JNICALL Java_com_ys_mlx90641_Mlx90641_readTemperature
  (JNIEnv * env, jclass cls)
{
	jfloatArray floatArr = (*env)->NewFloatArray(env,193);
	static float mlx90641[193];
  	if (mlx90641_fd <= 0)
  		return NULL;
	else{
		MLX90641_Get_Temperature_Data(mlx90641_fd,mlx90641);
		(*env)->SetFloatArrayRegion(env,floatArr,0,193,mlx90641);
	}
	return floatArr;

}

#ifdef __cplusplus
}
#endif
#endif
