#include <stdio.h>     
#include <stdlib.h>
#include <sys/ioctl.h>  
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include "rtx2080ti.h"
#define DEVICE_NAME "/dev/rtx2080ti"

int main(int argc,char ** argv)
{
	int ret=0;
	int i=-1;
	int framerate=0x4;
	Rtx2080ti_readoutdata TempData={0};
	ret=OpenModule();
	ret=SetModuleFramerate(0x6);
	if(ret!=0) 
		printf("SetFramerate Failed");
	ret=ReadOutModuleData(&TempData);
	if(ret!=0) 
		printf("ReadOutMOduleData Failed");
	while(i++<524) 
		printf("%X ",((char *)&TempData)[i]);
	ParseTemperature(&TempData);
	i=-1;
	while(i++<255) 
		printf("%.2f ",temp.Pixel[i]);
	ReleaseModule();
	return 0;
	
}
