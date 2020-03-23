package com.ys.temperaturelib.utils;


import android.util.Log;

import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.heatmap.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;

public class DataFormatUtil {


    /**
     * 根据传入的宽高计算传入数据在View上的具体坐标及对应温度值
     *
     * @param temperature  温度数据集
     * @param viewWidth
     * @param viewHeight
     * @param matrixXCount 矩阵一行个数
     * @return
     */
    public static List<WeightedLatLng> getHeatMapData(TemperatureEntity temperature,
                                                      int viewWidth, int viewHeight,
                                                      int matrixXCount, int matrixYCount) {
        List<WeightedLatLng> latLngs = new ArrayList<>();
        int w = viewWidth / matrixXCount;
        int h = viewHeight / matrixYCount;
        if ((h > 1.5 * 2)) h = w;
        for (int i = 0; i < temperature.tempList.size(); i++) {
            int x = i % matrixXCount;//(DGYMCU90640.MATRIX_COUT_X - 1) - (i % DGYMCU90640.MATRIX_COUT_X)镜像显示
            int y = i / matrixXCount;
            float temp = temperature.tempList.get(i);
            //start 由于温度差值较小热力图显示噪点过多模糊不清，增加一个差值计算 拉大数值差异 方便显示
            //差值可自行调节
            float temp1 = Math.abs(temp - 20);
            temp = temp1 * 10 + temp;
            //end
            WeightedLatLng weightedLatLng = new WeightedLatLng((x * w + w * 3), (y * h) + h * 3, temp);
            latLngs.add(weightedLatLng);
        }
        return latLngs;
    }

    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
