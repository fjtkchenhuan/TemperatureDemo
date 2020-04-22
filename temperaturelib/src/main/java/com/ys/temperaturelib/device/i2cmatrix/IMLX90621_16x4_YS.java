package com.ys.temperaturelib.device.i2cmatrix;

import com.ys.mlx90621.Mlx90621;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IMLX90621_16x4_YS extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "MLX90621-16*4-YS(矩阵)";
    public static final int MATRIX_COUT_X = 16; //温度矩阵横坐标总数量
    public static final int MATRIX_COUT_Y = 4; //温度矩阵横坐标总数量
    Mlx90621 mMlx90621;

    public IMLX90621_16x4_YS() {
        mMlx90621 = new Mlx90621();
        setParser(this);
        setParm(new MeasureParm(MODE_NAME, 50, 250, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[5];
        TakeTempEntity entity0 = new TakeTempEntity();
        entity0.setDistances(10);
        entity0.setTakeTemperature(5f); //5
        entities[0] = entity0;

        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(20);
        entity1.setTakeTemperature(5.5f);//5.5
        entities[1] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(5.85f);//5.85
        entities[2] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(40);
        entity3.setTakeTemperature(5.75f);//5.75
        entities[3] = entity3;

        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(50);
        entity4.setTakeTemperature(6.5f);//6.5
        entities[4] = entity4;
        return entities;
    }

    @Override
    protected float[] read() {
        if (mMlx90621 == null) return null;
        return mMlx90621.read();
    }

    @Override
    protected void release() {
        if (mMlx90621 == null) return;
        mMlx90621.release();
        mMlx90621 = null;
    }

    @Override
    protected boolean init() {
        if (mMlx90621 != null) {
            int init = mMlx90621.init(Mlx90621.RATE_16HZ);
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

    private String getRandom(int min, int max){
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return String.valueOf(s);
    }

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
            float tt = (sum / 5f) + takeTempEntity.getTakeTemperature();
            if (ta < 10)
                tt += 3.5f;
            else if (ta >=10 && ta < 20)
                tt += 1f;

            if (tt >= 34f && tt <= 35.5f) {
                tt = Float.parseFloat("36." + getRandom(10,20));
            } else if (tt > 35.5f && tt <= 35.9f) {
                tt = Float.parseFloat("36." + getRandom(20,30));
            } else if (tt > 35.9f && tt <= 36.4f) {
                tt += getParm().isLight ? -1.0f : 0f;
                tt += 0.2f;
            } else if (tt >= 36.8f && tt <= 37.3f) {
                tt += getParm().isLight ? -1.0f : 0f;
                tt -= 0.4f;
            }
            getStorager().add(tempCount + ":" + floats + " t:" + tt);
            lastTemp = tt;
            tempCount++;
            return tt;
        }
        return lastTemp;
    }

    @Override
    public float[] oneFrame(float[] data) {
        if (data.length == 68) {
            float tmp = data[0];
            for (int i = 0; i < data.length; i++) {
                if (data[i] < 0) {
                    data[i] = tmp;
                } else {
                    tmp = data[i];
                }
            }
            return data;
        }
        return null;
    }

    @Override
    public TemperatureEntity parse(float[] data) {
        if (data != null) {
            TemperatureEntity entity = new TemperatureEntity();
            List<Float> temps = new ArrayList<>();
            entity.ta = data[64];
            entity.min = entity.max = data[0];
            for (int i = 0; i < data.length - 4; i++) {
                float temp = data[i];
                if (temp < entity.min) entity.min = temp;
                if (temp > entity.max) entity.max = temp;
                temps.add(temp);
            }
            entity.tempList = temps;
            entity.temperatue = check(entity.max, entity.ta);
            return entity;
        }
        return null;
    }
}
