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
	
#define MLX90621_IOCTL_MAGIC			'n'
#define MLX90621_GET_RAMDATA	_IOR(MLX90621_IOCTL_MAGIC, 1, int *)
#define MLX90621_GET_EEPROMDATA	_IOR(MLX90621_IOCTL_MAGIC, 2, int *)
#define MLX90621_SET_DATA	_IOR(MLX90621_IOCTL_MAGIC, 3, int *)

struct MLX90621_R_DATA{
	uint8_t reg[4];
	uint16_t size;
	uint8_t buff[256];
};

struct MLX90621_W_DATA{
	uint8_t size;
	uint8_t data[10];
};

struct MLX90621_W_DATA *globe_write_data;
struct MLX90621_R_DATA *globe_read_data;

int i2c_write(int fd,struct MLX90621_W_DATA *data);
int i2c_read_ram(int fd,struct MLX90621_R_DATA *data);
int i2c_read_eeprom(int fd,struct MLX90621_R_DATA *data);
int MLX90621_I2CRead_RAM(int fd,uint8_t *reg,uint16_t len,uint8_t *data);
int MLX90621_I2CRead_EEPROM(int fd,uint8_t *reg,uint16_t len,uint8_t *data);
int MLX90621_I2CWrite(int fd,uint8_t len,uint8_t *data);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Begin registers
#define CAL_ACOMMON_L 0xD0
#define CAL_ACOMMON_H 0xD1
#define CAL_ACP_L 0xD3
#define CAL_ACP_H 0xD4
#define CAL_BCP 0xD5
#define CAL_alphaCP_L 0xD6
#define CAL_alphaCP_H 0xD7
#define CAL_TGC 0xD8
#define CAL_AI_SCALE 0xD9
#define CAL_BI_SCALE 0xD9


#define VTH_L 0xDA
#define VTH_H 0xDB
#define KT1_L 0xDC
#define KT1_H 0xDD
#define KT2_L 0xDE
#define KT2_H 0xDF
#define KT_SCALE 0xD2

//Common sensitivity coefficients
#define CAL_A0_L 0xE0
#define CAL_A0_H 0xE1
#define CAL_A0_SCALE 0xE2
#define CAL_DELTA_A_SCALE 0xE3
#define CAL_EMIS_L 0xE4
#define CAL_EMIS_H 0xE5
#define CAL_KSTA_L 0xE6
#define CAL_KSTA_H 0xE7


//Config register = 0xF5-F6
#define OSC_TRIM_VALUE 0xF7

void I2C_readEeprom();
void I2C_writeDevConfig();
void I2C_writeOscTrim();
void I2C_readPTAT();
void I2C_readIRData();
void I2C_readPixelCompensation();
void preCalculateConstants();
void calculateTO();
void calculateTA();
int16_t twos_16(uint8_t highByte, uint8_t lowByte);
int8_t twos_8(uint8_t byte);
uint16_t unsigned_16(uint8_t highByte, uint8_t lowByte);
uint16_t I2C_readDevConfig();
void mlx90621_initialise(int rate);
void mlx90621_measure();
	
//////////////////////////////////
#endif
