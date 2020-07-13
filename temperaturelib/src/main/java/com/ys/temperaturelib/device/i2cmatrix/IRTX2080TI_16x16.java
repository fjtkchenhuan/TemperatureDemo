package com.ys.temperaturelib.device.i2cmatrix;

import android.util.Log;

import com.ys.mlx90621.Mlx90621;
import com.ys.rtx2080ti.Rtx2080ti;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;
import com.ys.temperaturelib.utils.DataFormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class IRTX2080TI_16x16 extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "RTX2080TI-16*16";
    public static final int MATRIX_COUT_X = 16; //温度矩阵横坐标总数量
    public static final int MATRIX_COUT_Y = 16; //温度矩阵横坐标总数量
    Rtx2080ti rtx2080ti;

    public IRTX2080TI_16x16() {
        rtx2080ti = new Rtx2080ti();
        setParser(this);
        setParm(new MeasureParm(MODE_NAME, 50, 250, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    protected float[] read() {
        if (rtx2080ti == null) return null;
        return rtx2080ti.read();
    }

    @Override
    protected void release() {
        if (rtx2080ti == null) return;
        rtx2080ti.release();
        rtx2080ti = null;
    }

    @Override
    protected boolean init() {
        if (rtx2080ti != null) {
            int init = rtx2080ti.init(Rtx2080ti.RATE_16HZ);
            return init > 0;
        }
        return false;
    }

    @Override
    public void order(byte[] data) {

    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[5];
        TakeTempEntity entity0 = new TakeTempEntity();
        entity0.setDistances(5);
        entity0.setTakeTemperature(-5.15f);//-1.25   -1.3
        entities[0] = entity0;

        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(20);
        entity1.setTakeTemperature(-1.1f);//0.05   -0.5
        entities[1] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(-0.25f);//-0.15  -0.4
        entities[2] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(40);
        entity3.setTakeTemperature(0.55f);//0.6   -0.25
        entities[3] = entity3;

        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(50);
        entity4.setTakeTemperature(1.15f);//1.45  -0.4
        entities[4] = entity4;
        return entities;
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

            float tt = sum / 5f + takeTempEntity.getTakeTemperature();
//            if (tt >= 34f && tt < 36f) {
//                int tt1 = (int) (tt * 100);
//                tt = Float.parseFloat("36." + String.valueOf(tt1).substring(2, 4));
//            } else if (tt >= 37.2f && tt <= 37.5f) {
//                tt += 0.3f;
//            }
            if (getStorager() != null)
//                getStorager().add(tempCount + ":" + floats + " t:" + tt);
            lastTemp = tt;
            tempCount++;
            return tt;
        }
        return lastTemp;
    }

    @Override
    public float[] oneFrame(float[] data) {
        if (data.length == 257) {
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
            entity.ta = data[0];
            entity.min = entity.max = data[0];
            for (int i = 0; i < data.length - 4; i++) {
                float temp = data[i];
                if (temp < entity.min) entity.min = temp;
                if (temp > entity.max) entity.max = temp;
                temps.add(temp);
            }
//            getStorager().add(" List=" + temps + "\n");
            entity.tempList = temps;
            entity.temperatue = check(entity.max, entity.ta);
            return entity;
        }
        return null;
    }
}
