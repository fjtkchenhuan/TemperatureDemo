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
#ifndef _AMG88XX_API_H_
#define _AMG88XX_API_H_
    
#include <stdio.h>     
#include <stdlib.h>
#include <sys/ioctl.h>  
#include <unistd.h>
#include <fcntl.h>
#include <string.h>	
	
#define DRIVER_NAME 	"amg88xx"
#define AMG88XX_IOCTL_MAGIC			'a'
#define AMG88XX_GET_DATA	_IOR(AMG88XX_IOCTL_MAGIC, 1, int *)
#define AMG88XX_SET_DATA	_IOR(AMG88XX_IOCTL_MAGIC, 2, int *)

struct AMG88XX_R_DATA{
	uint8_t reg;
	uint16_t size;
	uint8_t buff[128];
};

struct AMG88XX_W_DATA{
	uint8_t size;
	uint8_t data[2];
};

struct AMG88XX_W_DATA *globe_write_data;
struct AMG88XX_R_DATA *globe_read_data;

int i2c_write(int fd,struct AMG88XX_W_DATA *data);
int i2c_read(int fd,struct AMG88XX_R_DATA *data);
int AMG88XX_I2CRead_Data(int fd,uint8_t reg,uint16_t len,uint8_t *data);
int AMG88XX_I2CWrite(int fd,uint8_t len,uint8_t *data);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#define AMG88xx_THERMISTOR_CONVERSION (0.0625)
#define AMG88xx_PIXEL_TEMP_CONVERSION (0.25)
void amg88xx_initialise(int rate);
float signedMag12ToFloat(uint16_t val);
void amg88xx_readThermistor();
void amg88xx_measure(); 
void find_max_min();	
//////////////////////////////////
#endif
