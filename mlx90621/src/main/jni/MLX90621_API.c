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
#include "MLX90621_API.h"
#include <math.h>

extern int mlx90621_fd;

uint8_t u8stCmd[5];
int16_t irData[64]; //Contains the raw IR data from the sensor
float temperatures[64+4]; //Contains the calculated temperatures of each pixel in the array
float Tambient; //Tracks the changing ambient temperature of the sensor

float v_ir_off_comp, ksta, v_ir_tgc_comp, v_ir_comp, alpha_comp;
float tak4, resolution_comp;
int16_t a_common, a_i_scale, b_i_scale, k_t1_scale, k_t2_scale, resolution;
uint8_t eepromData[256]; //Contains the full EEPROM reading from the MLX90621
float k_t1, k_t2, emissivity, tgc, alpha_cp, a_cp, b_cp, v_th;
uint16_t ptat;
int16_t cpix;
float a_ij, b_ij, alpha_ij;
float minTemp, maxTemp;
uint16_t refreshRate = 1;
int i2c_write(int fd,struct MLX90621_W_DATA *data)
{
	int ret = ioctl(fd,MLX90621_SET_DATA,data);
	if(ret < 0){
		printf("===== MLX90621_SET_DATA Fail=====\n");
		return -1;
	}
	return 0;
}

int i2c_read_ram(int fd,struct MLX90621_R_DATA *data)
{
	int ret = ioctl(fd,MLX90621_GET_RAMDATA,data);
	if(ret < 0){
		printf("===== MLX90621_GET_RAMDATA Fail=====\n");
		return -1;
	}
	return 0;
}

int i2c_read_eeprom(int fd,struct MLX90621_R_DATA *data)
{
	int ret = ioctl(fd,MLX90621_GET_EEPROMDATA,data);
	if(ret < 0){
		printf("===== MLX90621_GET_EEPROMDATA Fail=====\n");
		return -1;
	}
	return 0;
}


int MLX90621_I2CRead_RAM(int fd,uint8_t *reg,uint16_t len,uint8_t *data)
{
	int ret = 0;
	globe_read_data->reg[0] = reg[0];	//command
	globe_read_data->reg[1] = reg[1];	//address
	globe_read_data->reg[2] = reg[2];	//step
	globe_read_data->reg[3] = len>>1;	//len/2	
	globe_read_data->size = len;		//numberRead
	memset(&globe_read_data->buff[0],0,256);
    ret = i2c_read_ram(fd,globe_read_data);
	
	if(0 == ret){
		memcpy(data,&globe_read_data->buff[0],len);
		return 0;
	}
	else{
		return -1;
	}
}

int MLX90621_I2CRead_EEPROM(int fd,uint8_t *reg,uint16_t len,uint8_t *data)
{
	int ret = 0;

	globe_read_data->reg[0] = reg[0];	//command
	globe_read_data->size = len;		//numberRead
	memset(&globe_read_data->buff[0],0,256);
    ret = i2c_read_eeprom(fd,globe_read_data);
	
	if(0 == ret){
		memcpy(data,&globe_read_data->buff[0],len);
		return 0;
	}
	else{
		return -1;
	}
	
}

int MLX90621_I2CWrite(int fd,uint8_t len,uint8_t *data)
{
	int ret = 0;
	globe_write_data->data[0] = data[0];	
	globe_write_data->data[1] = data[1];
	globe_write_data->data[2] = data[2];
	globe_write_data->data[3] = data[3];
	globe_write_data->data[4] = data[4];
	globe_write_data->size = len;
	ret = i2c_write(fd,globe_write_data);

	if(0 == ret){
		return 0;
	}
	else{
		return -1;
	}
}	
  
void mlx90621_initialise(int rate) {
	refreshRate = rate;
	I2C_readEeprom();
	I2C_writeDevConfig();
	I2C_writeOscTrim();	
	preCalculateConstants();
}

void mlx90621_measure() {
	//Poll the MLX90621 for its current status Returns true if the POR/Brown out bit is set	
	int check = ((I2C_readDevConfig() & 0x0400) >> 10);
	if (1 == check) {
		I2C_readEeprom();
		I2C_writeDevConfig();
		I2C_writeOscTrim();
	}
	I2C_readPTAT();
	I2C_readIRData();

	calculateTA();
	I2C_readPixelCompensation();
	calculateTO();

	temperatures[64] = Tambient;
	temperatures[65] = minTemp;
	temperatures[66] = maxTemp;
}  
  

void I2C_writeDevConfig() {
	uint8_t Hz_LSB;
	uint8_t defaultConfig_H = 0x46;  //kmoto: See data sheet p.11 and 25
	
	switch (refreshRate) {
		case 0:
			Hz_LSB = 0x3F;
			break;
		case 1:
			Hz_LSB = 0x3E;
			break;
		case 2:
			Hz_LSB = 0x3D;
			break;
		case 4:
			Hz_LSB = 0x3C;
			break;
		case 8:
			Hz_LSB = 0x3B;
			break;
		case 16:
			Hz_LSB = 0x3A;
			break;
		case 32:
			Hz_LSB = 0x39;
			break;
		default:
			Hz_LSB = 0x38;
	}

	u8stCmd[0] = 0x03;	
	u8stCmd[1] = Hz_LSB - 0x55;
	u8stCmd[2] = Hz_LSB;
	u8stCmd[3] = defaultConfig_H - 0x55;
	u8stCmd[4] = defaultConfig_H;
	MLX90621_I2CWrite(mlx90621_fd,5,u8stCmd);
	resolution = (I2C_readDevConfig() & 0x30) >> 4;
}

void I2C_readEeprom() {
	u8stCmd[0] = 0x00;
	MLX90621_I2CRead_EEPROM(mlx90621_fd,u8stCmd,256,eepromData);
#if 0
	printf("eeprom[0xf7] = 0x%x ",eepromData[OSC_TRIM_VALUE]);
	printf("eeprom[0xf6~0xf5] = 0x%x%x \n",eepromData[0xf6],eepromData[0xf5]);
#endif	
}

void I2C_writeOscTrim() {
	u8stCmd[0] = 0x04;
	u8stCmd[1] = (eepromData[OSC_TRIM_VALUE]) - 0xAA;    
	u8stCmd[2] = eepromData[OSC_TRIM_VALUE];   
	u8stCmd[3] = (uint8_t)(eepromData[OSC_TRIM_VALUE] >> 8) - 0xAA;
	u8stCmd[4] = (uint8_t)(eepromData[OSC_TRIM_VALUE] >> 8);
	MLX90621_I2CWrite(mlx90621_fd,5,u8stCmd);
}

uint16_t I2C_readDevConfig() {
	uint8_t u8ConfigValue[2];
	u8stCmd[0] = 0x02;
	u8stCmd[1] = 0x92;
	u8stCmd[2] = 0x00;
	MLX90621_I2CRead_RAM(mlx90621_fd,u8stCmd,2,u8ConfigValue);
	uint16_t value;
	value = (u8ConfigValue[1] << 8) | u8ConfigValue[0]; 
#if 0
	printf("I2C_readDevConfig is 0x%x\n",value);
#endif
	return value;	
}

void calculateTA(void) {
	Tambient = ((-k_t1 + sqrt(k_t1*k_t1 - (4 * k_t2 * (v_th - (float) ptat))))
			/ (2 * k_t2)) + 25.0;
}

void preCalculateConstants() {
	resolution_comp = pow(2.0, (3 - resolution));
	emissivity = unsigned_16(eepromData[CAL_EMIS_H], eepromData[CAL_EMIS_L]) / 32768.0;
	a_common = twos_16(eepromData[CAL_ACOMMON_H], eepromData[CAL_ACOMMON_L]);
	a_i_scale = (int16_t)(eepromData[CAL_AI_SCALE] & 0xF0) >> 4;
	b_i_scale = (int16_t) eepromData[CAL_BI_SCALE] & 0x0F;

	alpha_cp = unsigned_16(eepromData[CAL_alphaCP_H], eepromData[CAL_alphaCP_L]) /
			   (pow(2.0, eepromData[CAL_A0_SCALE]) * resolution_comp);
	a_cp = (float) twos_16(eepromData[CAL_ACP_H], eepromData[CAL_ACP_L]) / resolution_comp;
	b_cp = (float) twos_8(eepromData[CAL_BCP]) / (pow(2.0, (float)b_i_scale) * resolution_comp);
	tgc = (float) twos_8(eepromData[CAL_TGC]) / 32.0;

	k_t1_scale = (int16_t) (eepromData[KT_SCALE] & 0xF0) >> 4;
	k_t2_scale = (int16_t) (eepromData[KT_SCALE] & 0x0F) + 10;
	v_th = (float) twos_16(eepromData[VTH_H], eepromData[VTH_L]);
	v_th = v_th / resolution_comp;
	k_t1 = (float) twos_16(eepromData[KT1_H], eepromData[KT1_L]);
	k_t1 /= (pow(2, k_t1_scale) * resolution_comp);
	k_t2 = (float) twos_16(eepromData[KT2_H], eepromData[KT2_L]);
	k_t2 /= (pow(2, k_t2_scale) * resolution_comp);
}

void calculateTO() {
	float v_cp_off_comp = (float) cpix - (a_cp + b_cp * (Tambient - 25.0));
	tak4 = pow((float) Tambient + 273.15, 4.0);
	minTemp = 0, maxTemp = 0;
	for (int i = 0; i < 64; i++) {
		a_ij = ((float) a_common + eepromData[i] * pow(2.0, a_i_scale)) / resolution_comp;
		b_ij = (float) twos_8(eepromData[0x40 + i]) / (pow(2.0, b_i_scale) * resolution_comp);
		v_ir_off_comp = (float) irData[i] - (a_ij + b_ij * (Tambient - 25.0));
		v_ir_tgc_comp = (float) v_ir_off_comp - tgc * v_cp_off_comp;
		float alpha_ij = ((float) unsigned_16(eepromData[CAL_A0_H], eepromData[CAL_A0_L]) / pow(2.0, (float) eepromData[CAL_A0_SCALE]));
		alpha_ij += ((float) eepromData[0x80 + i] / pow(2.0, (float) eepromData[CAL_DELTA_A_SCALE]));
		alpha_ij = alpha_ij / resolution_comp;
		//ksta = (float) twos_16(eepromData[CAL_KSTA_H], eepromData[CAL_KSTA_L]) / pow(2.0, 20.0);
		//alpha_comp = (1 + ksta * (Tambient - 25.0)) * (alpha_ij - tgc * alpha_cp);
		alpha_comp = (alpha_ij - tgc * alpha_cp);  	// For my MLX90621 the ksta calibrations were 0
													// so I can ignore them and save a few cycles
		v_ir_comp = v_ir_tgc_comp / emissivity;
		float temperature = pow((v_ir_comp / alpha_comp) + tak4, 1.0 / 4.0) - 274.15;

		temperatures[i] = temperature;
		if (minTemp == 0 || temperature < minTemp) {
			minTemp = temperature;
		}
		if (maxTemp == 0 || temperature > maxTemp) {
			maxTemp = temperature;
		}
	}
}

void I2C_readIRData() {
	uint8_t u8IRValue[128];
	u8stCmd[0] = 0x02;
	u8stCmd[1] = 0x00;
	u8stCmd[2] = 0x01;
	MLX90621_I2CRead_RAM(mlx90621_fd,u8stCmd,128,u8IRValue);

	for(int i= 0;i<128;){
		if(0 == i%2){
			irData[i/2] = u8IRValue[i] | (u8IRValue[i+1] << 8);
		}
		i = i+2;
	}
}

void I2C_readPTAT() {
	uint8_t u8PTATValue[2];
	u8stCmd[0] = 0x02;
	u8stCmd[1] = 0x40;
	u8stCmd[2] = 0x00;
	MLX90621_I2CRead_RAM(mlx90621_fd,u8stCmd,2,u8PTATValue);
	ptat = (u8PTATValue[1] << 8) | u8PTATValue[0]; 
}

void I2C_readPixelCompensation() {
	uint8_t u8CompstPiexl[2];
	u8stCmd[0] = 0x02;
	u8stCmd[1] = 0x41;
	u8stCmd[2] = 0x00;
	MLX90621_I2CRead_RAM(mlx90621_fd,u8stCmd,2,u8CompstPiexl);
	cpix = (u8CompstPiexl[1] << 8) | u8CompstPiexl[0]; 
}

int16_t twos_16(uint8_t highByte, uint8_t lowByte){
	uint16_t combined_word = 256 * highByte + lowByte;
	if (combined_word > 32767)
		return (int16_t) (combined_word - 65536);
	return (int16_t) combined_word;
}

int8_t twos_8(uint8_t byte) {
	if (byte > 127)
		return (int8_t) byte - 256;
	return (int8_t) byte;
}

uint16_t unsigned_16(uint8_t highByte, uint8_t lowByte){
	return (highByte << 8) | lowByte;
}

