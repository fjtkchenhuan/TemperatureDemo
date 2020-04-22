package com.ys.temperaturelib.device.i2cmatrix;

import com.ys.omron.Omron;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class IOMRON_4x4 extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "OMRON-4*4(矩阵)";
    public static final int MATRIX_COUT_X = 4; //温度矩阵横坐标总数量
    public static final int MATRIX_COUT_Y = 4; //温度矩阵横坐标总数量
    Omron mOmron;

    public IOMRON_4x4() {
        mOmron = new Omron();
        setParser(this);
        setParm(new MeasureParm(MODE_NAME, 50, 300, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[5];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(1.55f);//0.3 0.6  -0.25  0.9
        entities[0] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(20);
        entity2.setTakeTemperature(2.35f);//2.1 0.5  -0.95  0.7
        entities[1] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(30);
        entity3.setTakeTemperature(2.65f);//5  0.4  -2.75  0
        entities[2] = entity3;

        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(40);
        entity4.setTakeTemperature(4.6f);//7.2  -0.8  -1.35  -0.45
        entities[3] = entity4;

        TakeTempEntity entity5 = new TakeTempEntity();
        entity5.setDistances(50);
        entity5.setTakeTemperature(4.8f);//8.7 -2.65  -0.4  -0.85
        entities[4] = entity5;

        return entities;
    }

    @Override
    public float check(float value, float ta) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        return value + takeTempEntity.getTakeTemperature();
    }

    @Override
    protected float[] read() {
        if (mOmron == null) return null;
        int[] read = mOmron.read(Omron.OMRON_44L_SENSOR);
        if (read == null) return null;
        float[] temps = new float[read.length];
        for (int i = 0; i < read.length; i++) {
            temps[i] = read[i] / 10f;
        }
        return temps;
    }

    @Override
    protected void release() {
        if (mOmron == null) return;
        mOmron.release();
        mOmron = null;
    }

    @Override
    protected boolean init() {
        if (mOmron != null) {
            int init = mOmron.init(Omron.OMRON_44L_SENSOR);
            return init > 0;
        }
        return false;
    }

    @Override
    public void order(byte[] data) {

    }

    @Override
    public float[] oneFrame(float[] data) {
        return data;
    }

    @Override
    public TemperatureEntity parse(float[] data) {
        if (data != null) {
            TemperatureEntity entity = new TemperatureEntity();
            List<Float> temps = new ArrayList<>();
            entity.ta = data[16];
            entity.min = entity.max = check(data[0], entity.ta);
            for (int i = 0; i < data.length - 3; i++) {
                float temp = check(data[i], entity.ta);
                if (temp < entity.min) entity.min = temp;
                if (temp > entity.max) entity.max = temp;
                temps.add(temp);
            }
            entity.tempList = temps;
            entity.temperatue = entity.max;
            return entity;
        }
        return null;
    }
}
