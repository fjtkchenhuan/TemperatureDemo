#define RTX2080TI_IOCTL_MAGIC			'n'
#define RTX2080TI_GET_TEMPDATA	_IOR(RTX2080TI_IOCTL_MAGIC, 1, int *)
#define RTX2080TI_SET_FRAMERATE	_IOR(RTX2080TI_IOCTL_MAGIC, 2, int *)
#define DEVICE_NAME "/dev/rtx2080ti"
typedef unsigned char UCHAR;
#include <stdio.h>     
#include <stdlib.h>
#include <sys/ioctl.h>  
#include <unistd.h>
#include <fcntl.h>
#include <string.h>

struct rtx2080ti_header //前九个字节是模块返回的Header部分
{
	UCHAR Delimeter;//开始标志位
	UCHAR Respond;
	UCHAR Reserved;
	UCHAR Array_size:4;
	UCHAR Sequence:4;
	UCHAR	Reserved2[4];
	UCHAR Stop;//结束标志位
};

struct rtx2080ti_ambient //环境温度
{
	UCHAR AMB_H;
	UCHAR AMB_L;
	//WORD AMB;
	UCHAR reserved[2];
};
struct rtx2080ti_pixel //单传感器温度
{
	UCHAR OBJ_H;
	UCHAR OBJ_L;
};
typedef struct rtx2080ti_readoutdata
{
	struct rtx2080ti_header Header;
	struct rtx2080ti_ambient Ambient;
	struct rtx2080ti_pixel Pixel[256]; //256个传感器温度数据
}Rtx2080ti_readoutdata;
typedef Rtx2080ti_readoutdata * PRtx2080ti_readoutdata;
typedef struct rtx2080ti_framerate //设置温度帧率
{
	UCHAR CMD;
	UCHAR CFG1;
	UCHAR CFG2;
}RTX2080TI_FRAMERATE,*PRTX2080TI_FRAMERATE;

typedef struct rtx2080ti_temp
{
	float AMB;
	float Pixel[256];
}RTX2080TITEMP,*PRTX2080TITEMP;
extern int ParseTemperature(PRtx2080ti_readoutdata data);
extern int ReadOutModuleData(PRtx2080ti_readoutdata data);
extern int SetModuleFramerate(int framerate);
extern int OpenModule();
extern int ReleaseModule();
extern RTX2080TITEMP temp;
extern int fd;
