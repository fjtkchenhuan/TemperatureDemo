package com.ys.rtx2080ti;

public class Rtx2080ti {
    /**
     * 0:2Hz  1:4Hz  2:8Hz  3:16Hz
     */
    public static final int RATE_0HZ = 0;
    public static final int RATE_1HZ = 1;
    public static final int RATE_2HZ = 2;
    public static final int RATE_4HZ = 4;
    public static final int RATE_8HZ = 8;
    public static final int RATE_16HZ = 16;
    public static final int RATE_32HZ = 32;	

    /**
     * 初始化
     *
     * @param rate 刷新频率
     * @return
     */
    public int init(int rate) {
        return open(rate);
    }


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
        System.loadLibrary("rtx2080ti");
    }
}