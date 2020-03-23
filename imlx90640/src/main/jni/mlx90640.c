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

#include "MLX90640_API.h"

#define  LOG_TAG  "mlx90640"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define DEVICE_NAME "/dev/mlx90640"
#define  Rate2HZ   0x02
#define  Rate4HZ   0x03
#define  Rate8HZ   0x04
#define  Rate16HZ  0x05
#define  Rate32HZ  0x06

#define	 RefreshRate Rate16HZ
#define  TA_SHIFT 8 //Default shift for MLX90640 in open air

struct w_SE_DATA write_data;
struct SE_DATA read_data;
int mlx90640_fd = -1;
paramsMLX90640 mlx90640;

#ifndef _Included_com_ys_temperaturei2c_MLX90640
#define _Included_com_ys_temperaturei2c_MLX90640
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_ys_temperaturei2c_Mlx90640_open
  (JNIEnv * env, jclass cls,int value)
{
	uint16_t i = 0;
	uint16_t eeMLX90640[832]; 
	uint8_t rate = 0;
    if(mlx90640_fd > 0)
		return mlx90640_fd;

	globe_write_data = &write_data;
	globe_read_data = &read_data;
	mlx90640_fd = open(DEVICE_NAME, O_RDWR);  
	printf("mlx90640 open mlx90640_fd=%d\n",mlx90640_fd);
	if(mlx90640_fd > 0){
		if(0 == value)
			rate = Rate2HZ;
		else if(1 == value)
			rate = Rate4HZ;
		else if(2 == value)
			rate = Rate8HZ;
		else
			rate = Rate16HZ;
		MLX90640_SetRefreshRate(mlx90640_fd,rate);
		MLX90640_SetChessMode(mlx90640_fd);
		MLX90640_SetInit(mlx90640_fd,3);
		MLX90640_DumpEE(mlx90640_fd, eeMLX90640);
		MLX90640_ExtractParameters(eeMLX90640, &mlx90640);
		return mlx90640_fd;
	}
	else{
        	printf("Failed to open device %s\n", DEVICE_NAME);
        	return -1;
	}
	
}

JNIEXPORT void JNICALL Java_com_ys_temperaturei2c_Mlx90640_close
  (JNIEnv * env, jclass cls)
{
    if (mlx90640_fd > 0){
		   fflush(stdout); 
           close(mlx90640_fd);
           mlx90640_fd = -1;
    }
    return;
}

void MLX90640_Get_Temperature_Data(int fd,float *mlx90640To)
{
	uint16_t double_count = 0;
	uint16_t i=0,j=0;
	float Ta,tr;
	float emissivity=0.95;	
	float mlx90640To_Temp[768];
	uint16_t frame[834];
		
	for(double_count = 0; double_count < 2;){
		if(0x00 == MLX90640_GetFrameData(fd, frame)){
			Ta = MLX90640_GetTa(frame, &mlx90640);		
			tr = Ta - TA_SHIFT;
			printf("mlx90640 Ta:%f tr:%f\n",Ta,tr);
			if(0 == double_count)
				MLX90640_CalculateTo(frame, &mlx90640, emissivity, tr, mlx90640To);
			else
				MLX90640_CalculateTo(frame, &mlx90640, emissivity, tr, mlx90640To_Temp);
					
			if(1 == double_count){
				//merge data
				for(i = 0,j = 0;i<768;i++){		
					if(i%32 == 0 && i != 0){
						j++;
					}
					if(0 == j%2 && 1 == i%2){
						mlx90640To[i] = mlx90640To_Temp[i-1];
					}
					else if(1 == j%2 && 0 == i%2){
						mlx90640To[i] = mlx90640To_Temp[i+1];
					}
				}
				mlx90640To[768] = Ta;
				mlx90640To[769] = tr;
				#if 0
				printf("\n==========================Measure Temperature==========================\n");
				for(i = 0; i < 768; i++){
					if(i%32 == 0 && i != 0){
						printf("\n");
					}
					printf("%2.2f ",mlx90640To[i]);
				}
				printf("\n==========================Measure Temperature==========================\n");
				#endif
			}			
			double_count++;	
		}
	}
}

JNIEXPORT jfloatArray JNICALL Java_com_ys_temperaturei2c_Mlx90640_readTemperature
  (JNIEnv * env, jclass cls)
{
	jfloatArray floatArr = (*env)->NewFloatArray(env,770);
	float mlx90640[770];
	int i = 0;
  	if (mlx90640_fd <= 0)
  		return NULL;
	else{
		MLX90640_Get_Temperature_Data(mlx90640_fd,mlx90640);
		(*env)->SetFloatArrayRegion(env,floatArr,0,770,mlx90640);
	}
	return floatArr;

}

#ifdef __cplusplus
}
#endif
#endif
