package com.ys.temperaturelib.device.i2cmatrix;

import com.ys.omron.Omron;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class IOMRON_32x32 extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "OMRON-32*32(矩阵)";
    public static final int MATRIX_COUT_X = 32; //温度矩阵横坐标总数量
    public static final int MATRIX_COUT_Y = 32; //温度矩阵横坐标总数量
    Omron mOmron;

    public IOMRON_32x32() {
        mOmron = new Omron();
        setParser(this);
        setParm(new MeasureParm(MODE_NAME, 50, 500, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[3];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(2.96f);
        entities[0] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(3.61f);
        entities[1] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(50);
        entity3.setTakeTemperature(4.82f);
        entities[2] = entity3;

        return entities;
    }
    @Override
    public float check(float value,float ta) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        return value + takeTempEntity.getTakeTemperature();
    }

    @Override
    protected float[] read() {
        if (mOmron == null) return null;
        int[] read = mOmron.read(Omron.OMRON_32L_SENSOR);
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
            int init = mOmron.init(Omron.OMRON_32L_SENSOR);
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
            entity.ta = data[1024];
            entity.min = entity.max = check(data[0],entity.ta);
            for (int i = 0; i < data.length - 3; i++) {
                float temp = check(data[i],entity.ta);
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
