/**
 * @copyright (C) 2017 Melexis N.V.
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
 *
 */
#ifndef _MLX640_API_H_
#define _MLX640_API_H_
    
#include <stdio.h>     
#include <stdlib.h>
#include <sys/ioctl.h>  
#include <unistd.h>
#include <fcntl.h>
#include <string.h>	
	
typedef struct
{
        int16_t kVdd;
        int16_t vdd25;
        float KvPTAT;
        float KtPTAT;
        uint16_t vPTAT25;
        float alphaPTAT;
        int16_t gainEE;
        float tgc;
        float cpKv;
        float cpKta;
        uint8_t resolutionEE;
        uint8_t calibrationModeEE;
        float KsTa;
        float ksTo[4];
        int16_t ct[4];
        float alpha[768];    
        int16_t offset[768];    
        float kta[768];    
        float kv[768];
        float cpAlpha[2];
        int16_t cpOffset[2];
        float ilChessC[3]; 
        uint16_t brokenPixels[5];
        uint16_t outlierPixels[5];  
} paramsMLX90640;
    
int MLX90640_DumpEE(int fd, uint16_t *eeData);
int MLX90640_GetFrameData(int fd, uint16_t *frameData);
int MLX90640_ExtractParameters(uint16_t *eeData, paramsMLX90640 *mlx90640);
float MLX90640_GetVdd(uint16_t *frameData, const paramsMLX90640 *params);
float MLX90640_GetTa(uint16_t *frameData, const paramsMLX90640 *params);
void MLX90640_GetImage(uint16_t *frameData, const paramsMLX90640 *params, float *result);
void MLX90640_CalculateTo(uint16_t *frameData, const paramsMLX90640 *params, float emissivity, float tr, float *result);
int MLX90640_SetResolution(int fd, uint8_t resolution);
int MLX90640_GetCurResolution(int fd);
int MLX90640_SetRefreshRate(int fd, uint8_t refreshRate);   
int MLX90640_GetRefreshRate(int fd);  
int MLX90640_GetSubPageNumber(uint16_t *frameData);
int MLX90640_GetCurMode(int fd); 
int MLX90640_SetInterleavedMode(int fd);
int MLX90640_SetChessMode(int fd);
    
	
#define MLX90640_IOCTL_MAGIC			'm'
#define MLX90640_GET_DATA	_IOR(MLX90640_IOCTL_MAGIC, 1, int *)
#define MLX90640_SET_DATA	_IOR(MLX90640_IOCTL_MAGIC, 2, int *)

struct SE_DATA{
	uint16_t reg;
	uint16_t size;
	uint16_t buff[1664];
};

struct w_SE_DATA{
	uint16_t reg;
	uint16_t size;
	uint16_t data;
};

struct w_SE_DATA *globe_write_data;
struct SE_DATA *globe_read_data;

int i2c_write(int fd,struct w_SE_DATA *data);
int i2c_read(int fd,struct SE_DATA *data);
int MLX90640_I2CRead(int fd,uint16_t reg,uint16_t len,uint16_t *data);
int MLX90640_I2CWrite(int fd,uint16_t reg,uint16_t data);

#endif
