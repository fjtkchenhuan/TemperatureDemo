package com.ys.temperaturelib.device.i2cmatrix;


import android.util.Log;

import com.ys.mlx90641.Mlx90641;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;
import com.ys.temperaturelib.utils.DataFormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IMLX90641_16x12 extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "MLX90641-16*12";
    static final int MATRIX_COUT_X = 16; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 12; //温度矩阵横坐标总数量
    Mlx90641 mMlx90641;

    public IMLX90641_16x12() {
        mMlx90641 = new Mlx90641();
        setParser(this);
        setParm(new MeasureParm(MODE_NAME, 50, 200, MATRIX_COUT_X, MATRIX_COUT_Y));
    }

    @Override
    protected float[] read() {
        if (mMlx90641 == null) return null;
        return mMlx90641.read();
    }

    @Override
    protected void release() {
        if (mMlx90641 == null) return;
        mMlx90641.release();
        mMlx90641 = null;
    }

    @Override
    protected boolean init() {
        if (mMlx90641 != null) {
            int init = mMlx90641.init(Mlx90641.RATE_8HZ);
            return init > 0;
        }
        return false;
    }

    @Override
    public void order(byte[] data) {

    }

    int count = 0;
    List<Float> mFloats = new ArrayList<>();
    float lastTemp = 0;
    int tempCount = 0;

    @Override
    public float check(float value, float ta) {
//        TakeTempEntity takeTempEntity = getTakeTempEntity();
//        if (!takeTempEntity.isNeedCheck()) return value;
        count++;
        mFloats.add(value);
        if (mFloats.size() == 6) {
            tempCount = 5;
        } else if (mFloats.size() > 6) {
            List<Float> floats = mFloats.subList(tempCount - 3, tempCount - 3 + 5);
            float sum = 0;
            float max = floats.get(0);
            float min = floats.get(0);

            for (int i = 0; i < floats.size(); i++) {
                sum += floats.get(i);
                if (floats.get(i) > max) max = floats.get(i);
                if (floats.get(i) < min) min = floats.get(i);
            }

            float tt = (sum - max)/ 4f ; //+ takeTempEntity.getTakeTemperature();
//            if (tt >= 34f && tt < 36f) {
//                int tt1 = (int) (tt * 100);
//                tt = Float.parseFloat("36." + String.valueOf(tt1).substring(2, 4));
//            } else if (tt >= 37.2f && tt <= 37.5f) {
//                tt += 0.3f;
//            }
//            getStorager().add(tempCount + ":" + floats + " t:" + tt);
            lastTemp = tt;
            tempCount++;
            return tt;
        }
        return lastTemp;
    }


    @Override
    public float[] oneFrame(float[] data) {
        if (data.length == 193) {
            return data;
        }
        return null;
    }

    @Override
    public TemperatureEntity parse(float[] data) {
        if (data != null) {
            float[] datas = new float[193];
            for (int i = 0; i < data.length; i++) {
                String temp = data[i] + "";
                temp = temp.substring(0,4);
                datas[i] = Float.parseFloat(temp);
            }
            data = datas;
            float[] toDatas = Arrays.copyOfRange(data,0,192);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("全部的温度点数：\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas, 0, 16))).append("\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,16,32)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,32,48)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,48,64)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,64,80)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,80,96)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,96,112)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,112,128)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,128,144)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,144,160)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,160,176)) + "\n");
            stringBuilder.append(Arrays.toString(Arrays.copyOfRange(toDatas,176,192)));
            TemperatureEntity entity = new TemperatureEntity();
            List<Float> temps = new ArrayList<>();
            entity.min = entity.max = data[0] > 42 ? 42 : data[0];
            boolean isvalid = true;
            for (int i = 0; i < data.length - 2; i++) {
                if (data[i] < 0) {
                    isvalid = false;
                    break;
                }
                float temp = data[i];
                if (temp < entity.min) entity.min = temp;
                if (temp > entity.max) entity.max = temp;
                temps.add(temp);
            }

            //比最大值小1度的点数
            int maxCount = getMinCount(entity.max,toDatas);
            double correctValue = 0;
            if (maxCount > 0 && maxCount < 10)
                correctValue = getCorrectData1(maxCount);
            else if (maxCount > 10 && maxCount < 20)
                correctValue = getCorrectData2(maxCount);
            else if (maxCount > 20 && maxCount < 46)
                correctValue = getCorrectData3(maxCount);
            entity.max += correctValue;
            Log.d("sky","maxCount = " + maxCount + ", correctValue = " + correctValue);

            entity.tempList = temps;
            entity.ta = data[192];
            entity.temperatue = check(entity.max , entity.ta);

            stringBuilder.append("\nTO： " + entity.temperatue);
            stringBuilder.append("\nTA： " + entity.ta);
            stringBuilder.append("\nmax：" + entity.max);
            stringBuilder.append("\nmin：" + entity.min);
            stringBuilder.append("\n补偿的值：" + correctValue);
            stringBuilder.append("\n比TA值大1度之内的点数:" + getMaxCount(entity.ta,toDatas));
            stringBuilder.append("\n比max小1度之内的点数：" + maxCount);
            stringBuilder.append("\n比min大1度之内的点数：" + getMaxCount(entity.min,toDatas) + "\n");
            entity.sb = stringBuilder.toString();

            if (getStorager() != null)
//                getStorager().add(stringBuilder.toString());
            return isvalid ? entity : null;
        }
        return null;
    }

    //获取比指定温度小1度内的点数
    private int getMinCount(float target,float[] data) {
        int count = 0;
        for (float d : data) {
            if ((target-d) > 0 && (target-d) < 1)
                count ++;
        }
        return count;
    }

    //获取比指定温度大1度内的点数
    private int getMaxCount(float target,float[] data) {
        int count = 0;
        for (float d : data) {
            if ((d - target) > 0 && (d - target) < 1)
                count ++;
        }
        return count;
    }

    private double getCorrectData1(int count) {
        return 0.025 * count + 0.675;
    }

    private double getCorrectData2(int count) {
        return 0.033 * count + 0.07;
    }

    private double getCorrectData3(int count) {
        return 0.012 * count - 0.14;
    }
}
