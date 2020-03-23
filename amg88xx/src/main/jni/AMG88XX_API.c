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
#include "AMG88XX_API.h"
#include <math.h>

extern int amg88xx_fd;
uint8_t write_and_read[4];
float thermistor = 0;
float pixels[68];
float temp_max = 0;
float temp_min = 0;

int i2c_write(int fd,struct AMG88XX_W_DATA *data)
{
	int ret = ioctl(fd,AMG88XX_SET_DATA,data);
	if(ret < 0){
		printf("===== AMG88XX_SET_DATA Fail=====\n");
		return -1;
	}
	return 0;
}

int i2c_read(int fd,struct AMG88XX_R_DATA *data)
{
	int ret = ioctl(fd,AMG88XX_GET_DATA,data);
	if(ret < 0){
		printf("===== AMG88XX_GET_DATA Fail=====\n");
		return -1;
	}
	return 0;
}



int AMG88XX_I2CRead_Data(int fd,uint8_t reg,uint16_t len,uint8_t *data)
{
	int ret = 0;

	globe_read_data->reg = reg;
	globe_read_data->size = len;
	memset(&globe_read_data->buff[0],0,128);
    ret = i2c_read(fd,globe_read_data);
	
	if(0 == ret){
		memcpy(data,&globe_read_data->buff[0],len);
		return 0;
	}
	else{
		return -1;
	}
	
}

int AMG88XX_I2CWrite(int fd,uint8_t len,uint8_t *data)
{
	int ret = 0;
	globe_write_data->data[0] = data[0];	
	globe_write_data->data[1] = data[1];
	globe_write_data->size = len;
	ret = i2c_write(fd,globe_write_data);
	if(0 == ret){
		return 0;
	}
	else{
		return -1;
	}
}
  
void amg88xx_initialise(int rate) 
{
	memset(write_and_read,0,sizeof(write_and_read));
	//enter normal mode
	write_and_read[0] = 0x00;	
	write_and_read[1] = 0x00;
	AMG88XX_I2CWrite(amg88xx_fd,2,write_and_read);
	//software reset
	write_and_read[0] = 0x01;	
	write_and_read[1] = 0x3F;
	AMG88XX_I2CWrite(amg88xx_fd,2,write_and_read);	
	//disableInterrupt
	write_and_read[0] = 0x03;	
	write_and_read[1] = 0x00;
	AMG88XX_I2CWrite(amg88xx_fd,2,write_and_read);	
	//set to 10 FPS
	write_and_read[0] = 0x02;	
	if(1 == rate)
		write_and_read[1] = 0x01;//10fps:0x00  1fps:0x01
	else
		write_and_read[1] = 0x00;//10fps:0x00  1fps:0x01
	
	AMG88XX_I2CWrite(amg88xx_fd,2,write_and_read);		
}

float signedMag12ToFloat(uint16_t val)
{
	//take first 11 bits as absolute val
	uint16_t absVal = (val & 0x7FF);
	return (val & 0x8000) ? 0 - (float)absVal : (float)absVal ;
}

void amg88xx_readPixels()
{
	int i = 0;
	uint8_t rawArray[128];
	uint16_t recast;
	float converted;
		
	AMG88XX_I2CRead_Data(amg88xx_fd,0x80,128,rawArray);
	
	for(i = 0;i < 64;i++){
		uint8_t pos = i << 1;
		recast = ((uint16_t)rawArray[pos + 1] << 8) | ((uint16_t)rawArray[pos]);
		
		converted = signedMag12ToFloat(recast) * AMG88xx_PIXEL_TEMP_CONVERSION;
		pixels[i] = converted;		
	}
}

void amg88xx_readThermistor()
{
	AMG88XX_I2CRead_Data(amg88xx_fd,0x0E,2,write_and_read);
	uint16_t recast = ((uint16_t)write_and_read[1] << 8) | ((uint16_t)write_and_read[0]);
	thermistor = signedMag12ToFloat(recast) * AMG88xx_THERMISTOR_CONVERSION;
}

void amg88xx_measure()
{
	amg88xx_readThermistor();
	amg88xx_readPixels();
	find_max_min();
	pixels[64] = thermistor;
	pixels[65] = temp_min;
	pixels[66] = temp_max;
	printf("Ta:%f min:%f max:%f \n",thermistor,temp_min,temp_max);

}  

void find_max_min()
{
	temp_max = pixels[0];
	temp_min = pixels[0];
	for(int i = 0;i < 64;i++)
	{
	    if (pixels[i] > temp_max)
		{
			temp_max = pixels[i];
		}
		if (temp_min > pixels[i])
		{
			temp_min = pixels[i];
		}
	}
}
