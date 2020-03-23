package com.ys.omron;

public class Omron {

    public static final int OMRON_1A_SENSOR = 1;		//1x1 point
    public static final int OMRON_8L_SENSOR = 2;		//1x8 point
    public static final int OMRON_44L_SENSOR = 3;		//4x4 point
    public static final int OMRON_32L_SENSOR = 4;		//32x32 point
    /**
     * 初始化
     *
     * @param type 类型为:OMRON_1A_SENSOR	OMRON_8L_SENSOR		OMRON_44L_SENSOR	OMRON_32L_SENSOR
     * @return
     */
    public int init(int type) {
        return open(type);
    }

    /**
     * 读取Omron数据
     * OMRON_1A_SENSOR:		读取2个点		返回 [0] 		目标温度  			[1] 环境温度
     * OMRON_8L_SENSOR:		读取11个点		返回 [0-7] 		目标温度1x8个点		[8] 环境温度  		[9] 返回的最小温度			[10] 返回的最大温度
     * OMRON_44L_SENSOR:	读取19个点		返回 [0-15] 	目标温度4x4个点		[16] 环境温度 		[17] 返回的最小温度			[18] 返回的最大温度
     * OMRON_32L_SENSOR:	读取1027个点	返回 [0-1023] 	目标温度32x32个点	[1024] 环境温度		[1025] 返回的最小温度		[1026] 返回的最大温度
     * @return 返回数据
     */
    public int[] read(int type) {
		if(OMRON_1A_SENSOR == type){
			return readTemperature1A();
		}
		else if(OMRON_8L_SENSOR == type){
			return readTemperature8L();
		}
		else if(OMRON_44L_SENSOR == type){
			return readTemperature44L();
		}
		else{
			//OMRON_32L_SENSOR
			return readTemperature32L();
		}
    }

    /**
     * 释放设备
     */
    public void release() {
        close();
    }

    public static native int open(int rate);

    public static native void close();

    public static native int[] readTemperature1A();
    public static native int[] readTemperature8L();
	public static native int[] readTemperature44L();
	public static native int[] readTemperature32L();
	
    static {
        System.loadLibrary("omron");
    }
}