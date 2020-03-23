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
#include "OMROM_API.h"
#include <math.h>

extern int omron_fd;

int temperature[1024+3];
uint16_t temp_max = 0;
uint16_t temp_min = 0;
uint16_t Tambient = 0;

int i2c_write(int fd,struct omron_w_data *data)
{
	int ret = ioctl(fd,OMRON_SET_DATA,data);
	if(ret < 0){
		printf("===== OMRON_SET_DATA Fail=====\n");
		return -1;
	}
	return 0;
}

int i2c_read(int fd,struct omron_r_data *data)
{
	int ret = ioctl(fd,OMRON_GET_DATA,data);
	if(ret < 0){
		printf("===== OMRON_GET_DATA Fail=====\n");
		return -1;
	}
	return 0;
}

int OMROM_I2CRead(int fd,uint8_t reg,uint16_t len,uint8_t *data)
{
	int ret = 0;

	globe_read_data->reg = reg;
	globe_read_data->size = len;
	memset(&globe_read_data->buff[0],0,2052);
    ret = i2c_read(fd,globe_read_data);
	
	if(0 == ret){
		memcpy(data,&globe_read_data->buff[0],len);
		return 0;
	}
	else{
		return -1;
	}
	
}

int OMROM_I2CWrite(int fd,uint8_t len,uint8_t *data)
{
	int ret = 0;
	for(int i = 0; i< len;i++){
		globe_write_data->data[i] = data[i];
	}
	globe_write_data->size = len;
	ret = i2c_write(fd,globe_write_data);
	
	if(0 == ret){
		return 0;
	}
	else{
		return -1;
	}
}	
  
void omron_initialise(int type) {
	uint8_t value[5];
	if(OMRON_8L_SENSOR == type){
		value[0] = 0x02;
		value[1] = 0x00;
		value[2] = 0x01;
		value[3] = 0xEE;		
		OMROM_I2CWrite(omron_fd,4,value);

		value[0] = 0x05;
		value[1] = 0x90;
		value[2] = 0x3A;
		value[3] = 0xB8;		
		OMROM_I2CWrite(omron_fd,4,value);
		
		value[0] = 0x03;
		value[1] = 0x00;
		value[2] = 0x03;
		value[3] = 0x8B;		
		OMROM_I2CWrite(omron_fd,4,value);
		
		value[0] = 0x03;
		value[1] = 0x00;
		value[2] = 0x07;
		value[3] = 0x97;		
		OMROM_I2CWrite(omron_fd,4,value);
		
		value[0] = 0x02;
		value[1] = 0x00;
		value[2] = 0x00;
		value[3] = 0xE9;		
		OMROM_I2CWrite(omron_fd,4,value);
		sleep(1);	
	}
	else{
		sleep(1);
	}
}

uint8_t calc_crc(uint8_t data )
{
	int index;
	uint8_t temp;
	for(index=0;index<8;index++){
		temp = data;
		data <<= 1;
		if(temp & 0x80)
			data ^= 0x07;
	}
	return data;
}

int D6T_checkPEC(uint8_t *buf , int pPEC)
{
	unsigned char crc;
	int i;
	crc = calc_crc(0x14);
	crc = calc_crc(0x4C ^ crc);
	crc = calc_crc(0x15 ^ crc);
	for(i = 0;i < pPEC;i++){
		crc = calc_crc(buf[i] ^ crc );
	}
	//printf("harris D6T_checkPEC 0x%x 0x%x\n",crc,buf[pPEC]);
	if(crc == buf[pPEC])
		return 1;
	else
		return 0;
}

int D6T_checkPEC1(uint8_t *buf , int pPEC)
{
	unsigned char crc;
	int i;
	crc = calc_crc(0x15);
	for(i=0;i < pPEC;i++){
		crc = calc_crc(buf[i] ^ crc);
	}
	//printf("harris D6T_checkPEC1 0x%x 0x%x\n",crc,buf[pPEC]);
	if(crc == buf[pPEC])
		return 1;
	else
		return 0;	
}

void omron_measure(int type) {
	uint8_t irData[2052];
	uint16_t i = 0;
	memset(irData,0,2052);
	if(OMRON_8L_SENSOR == type){
		OMROM_I2CRead(omron_fd,0x4c,19,irData);
		if(1 == D6T_checkPEC(irData,18)){
			//1x8
			for(i = 2 ; i< 18;i += 2){
				temperature[(i-2)/2] = (irData[i+1]<<8)|irData[i];
			}
			Tambient = (irData[1]<<8)|irData[0];
			find_max_min(8);
			temperature[8] = Tambient;
			temperature[9] = temp_min;
			temperature[10] = temp_max;
		}
	}
	else if(OMRON_32L_SENSOR == type){
		//32x32
		OMROM_I2CRead(omron_fd,0x4c,2051,irData);//(1+1024)*2 = 2050
		if(1 == D6T_checkPEC1(irData,2050)){
			for(i = 2 ; i< 2050;i += 2){
				temperature[(i-2)/2] = (irData[i+1]<<8)|irData[i];
			}
			Tambient = (irData[1]<<8)|irData[0];
			find_max_min(1024);
			temperature[1024] = Tambient;
			temperature[1025] = temp_min;
			temperature[1026] = temp_max;
		}
	}
	else if(OMRON_44L_SENSOR == type){
		//4x4
		OMROM_I2CRead(omron_fd,0x4c,35,irData);
		if(1 == D6T_checkPEC(irData,34)){
			for(i = 2 ; i< 34;i += 2){
				temperature[(i-2)/2] = (irData[i+1]<<8)|irData[i];
			}
			Tambient = (irData[1]<<8)|irData[0];
			find_max_min(16);
			temperature[16] = Tambient;
			temperature[17] = temp_min;
			temperature[18] = temp_max;
		}		
	}
	else{
		//1
		OMROM_I2CRead(omron_fd,0x4c,5,irData);
		if(1 == D6T_checkPEC(irData,4)){
			temperature[0] = (irData[3]<<8)|irData[2];
			Tambient = (irData[1]<<8)|irData[0];
			temperature[1] = Tambient;
		}			
	}


}  
  
void find_max_min(int size)
{
	temp_max = temperature[0];
	temp_min = temperature[0];
	for(int i = 0;i < size;i++){
		if (temperature[i] > temp_max){
			temp_max = temperature[i];
		}
		if (temp_min > temperature[i]){
			temp_min = temperature[i];
		}
	}
}
