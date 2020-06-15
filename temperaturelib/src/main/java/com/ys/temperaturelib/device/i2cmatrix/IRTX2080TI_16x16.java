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
    public float check(float value, float ta) {
       return value;
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
            Log.d("sky","data = " + Arrays.toString(data));
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
