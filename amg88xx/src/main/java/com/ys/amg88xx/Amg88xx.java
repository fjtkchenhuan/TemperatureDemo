package com.ys.amg88xx;

public class Amg88xx {

    public static final int RATE_1HZ = 1;
    public static final int RATE_10HZ = 10;

    /**
     * 初始化
     *
     * @param rate 刷新频率
     * @return
     */
    public int init(int rate) {
        return open(rate);
    }

    /**
     * 读取Amg88xx数据 8*8
     * [0~63]	返回的8x8个点的温度数据
	 * [64]	返回的环境温度
	 * [65]	返回的最小温度
	 * [66]	返回的最大温度
     * @return 返回数据
     */
    public float[] read() {
        return readTemperature();
    }

    /**
     * 释放设备
     */
    public void release() {
        close();
    }

    public static native int open(int rate);

    public static native void close();

    public static native float[] readTemperature();

    static {
        System.loadLibrary("amg88xx");
    }
}