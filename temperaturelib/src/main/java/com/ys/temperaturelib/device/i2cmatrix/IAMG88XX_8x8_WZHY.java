package com.ys.temperaturelib.device.i2cmatrix;

import com.ys.amg88xx.Amg88xx;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class IAMG88XX_8x8_WZHY extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "AMG88XX-8*8-WZHY(矩阵)";
    public static final int MATRIX_COUT_X = 8; //温度矩阵横坐标总数量
    public static final int MATRIX_COUT_Y = 8; //温度矩阵横坐标总数量
    Amg88xx mAmg88xx;

    public IAMG88XX_8x8_WZHY() {
        mAmg88xx = new Amg88xx();
        setParser(this);
        setParm(new MeasureParm(MODE_NAME, 50, 150, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[3];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(5.13f);
        entities[0] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(20);
        entity2.setTakeTemperature(6.81f);
        entities[1] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(30);
        entity3.setTakeTemperature(8.12f);
        entities[2] = entity3;

        return entities;
    }

    @Override
    protected float[] read() {
        if (mAmg88xx == null) return null;
        return mAmg88xx.read();
    }

    @Override
    protected void release() {
        if (mAmg88xx == null) return;
        mAmg88xx.release();
        mAmg88xx = null;
    }

    @Override
    protected boolean init() {
        if (mAmg88xx != null) {
            int init = mAmg88xx.init(Amg88xx.RATE_10HZ);
            return init > 0;
        }
        return false;
    }

    @Override
    public void order(byte[] data) {

    }

    @Override
    public float check(float value, float ta) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        return value + takeTempEntity.getTakeTemperature();
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
            entity.min = entity.max = check(data[0], entity.ta);
            for (int i = 0; i < data.length - 4; i++) {
                if (data[i] <= 0) continue;
                float temp = check(data[i], entity.ta);
                if (temp < entity.min) entity.min = temp;
                if (temp > entity.max) entity.max = temp;
                temps.add(temp);
            }
            if (temps.isEmpty()) return null;
            entity.tempList = temps;
            entity.temperatue = entity.max;
            return entity;
        }
        return null;
    }
}
