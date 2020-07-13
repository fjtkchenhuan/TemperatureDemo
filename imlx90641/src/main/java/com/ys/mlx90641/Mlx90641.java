package com.ys.mlx90641;

public class Mlx90641 {
    /**
     * 0:2Hz  1:4Hz  2:8Hz  3:16Hz
     */
    public static final int RATE_2HZ = 0;
    public static final int RATE_4HZ = 1;
    public static final int RATE_8HZ = 2;
    public static final int RATE_16HZ = 3;

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
     * 读取Mlx90640数据
     *
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
        System.loadLibrary("mlx90641");
    }
}