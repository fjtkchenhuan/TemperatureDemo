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
#ifndef _OMRON_API_H_
#define _OMRON_API_H_
    
#include <stdio.h>     
#include <stdlib.h>
#include <sys/ioctl.h>  
#include <unistd.h>
#include <fcntl.h>
#include <string.h>	
	
#define DRIVER_NAME 	"omron"
#define OMRON_IOCTL_MAGIC	'm'
#define OMRON_GET_DATA	_IOR(OMRON_IOCTL_MAGIC, 1, int *)
#define OMRON_SET_DATA	_IOR(OMRON_IOCTL_MAGIC, 2, int *)

#define OMRON_1A_SENSOR 		(1) 	//1x1 point
#define OMRON_8L_SENSOR 		(2)		//1x8 point
#define OMRON_44L_SENSOR 		(3) 	//4x4 point
#define OMRON_32L_SENSOR 		(4)		//32x32 point

struct omron_r_data {
	uint8_t reg;
	uint16_t size;
	uint8_t buff[2052];
};

struct omron_w_data{
	uint8_t reg;
	uint16_t size;
	uint8_t data[10];
};

struct omron_w_data *globe_write_data;
struct omron_r_data *globe_read_data;

int i2c_write(int fd,struct omron_w_data *data);
int i2c_read_ram(int fd,struct omron_r_data *data);
int i2c_read_eeprom(int fd,struct omron_r_data *data);
int OMROM_I2CRead(int fd,uint8_t reg,uint16_t len,uint8_t *data);
int OMROM_I2CWrite(int fd,uint8_t len,uint8_t *data);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void omron_initialise(int type);
void omron_measure(int type);
void find_max_min(int size);	
//////////////////////////////////
#endif
