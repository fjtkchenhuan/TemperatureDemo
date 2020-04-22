package com.ys.temperaturelib.device.i2cmatrix;

import com.ys.temperaturei2c.Mlx90640;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class IMLX90640_32x24_OAA1586901 extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "MLX90640-32*24-OAA1586901(矩阵)";
    static final int MATRIX_COUT_X = 32; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 24; //温度矩阵横坐标总数量
    Mlx90640 mMlx90640;

    public IMLX90640_32x24_OAA1586901() {
        mMlx90640 = new Mlx90640();
        setParser(this);
        setParm(new MeasureParm(MODE_NAME, 50, 150, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[5];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(0.9f);//-0.2 -0.25  1.25  0.1
        entities[0] = entity1;
        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(20);
        entity2.setTakeTemperature(0.95f);//0.1 -0.15  1.35  -0.35
        entities[1] = entity2;
        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(30);
        entity3.setTakeTemperature(1.45f);//0.5 -0.1  1.1  -0.05
        entities[2] = entity3;
        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(40);
        entity4.setTakeTemperature(1.85f);//1.7  -1.1  1.25  0
        entities[3] = entity4;
        TakeTempEntity entity5 = new TakeTempEntity();
        entity5.setDistances(50);
        entity5.setTakeTemperature(2.0f);//2.55 -1.3  0.85  -0.1
        entities[4] = entity5;
        return entities;
    }
    @Override
    protected float[] read() {
        if (mMlx90640 == null) return null;
        return mMlx90640.read();
    }

    @Override
    protected void release() {
        if (mMlx90640 == null) return;
        mMlx90640.release();
        mMlx90640 = null;
    }

    @Override
    protected boolean init() {
        if (mMlx90640 != null) {
            int init = mMlx90640.init(Mlx90640.RATE_16HZ);
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
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
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
            float tt = (sum / 5f);
            tt += (31 - ta) * 0.077f;
            tt += getTakeTempEntity().getTakeTemperature();
//            if (tt >= 34f && tt <= 36f) {
//                int tt1 = (int) (tt * 100);
//                tt1 += getParm().isLight ? -1.0f : 0f;
//                tt = Float.parseFloat("36." + String.valueOf(tt1).substring(2, 4));
//            } else if (tt >= 37.2f && tt <= 37.5f) {
//                tt += getParm().isLight ? -1.0f : 0f;
//                tt += 0.3f;
//            }
            getStorager().add(tempCount + ":" + floats + " t:" + tt);
            lastTemp = tt;
            tempCount++;
            return tt;
        }
        return lastTemp;
    }

    @Override
    public float[] oneFrame(float[] data) {
        if (data.length == 770) {
            return data;
        }
        return null;
    }

    @Override
    public TemperatureEntity parse(float[] data) {
        if (data != null) {
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
            entity.tempList = temps;
            entity.ta = data[768];
            entity.temperatue = check(entity.max , entity.ta);
            return isvalid ? entity : null;
        }
        return null;
    }
}
