package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class SMLX90621_YS extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "MLX90621-YS(矩阵)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 115200; //波特率

    static final int MATRIX_COUT_X = 16; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 4; //温度矩阵纵坐标总数量

    static final byte[] ORDER_DATA_OUTPUT_AUTO = new byte[]{(byte) 0xA5, 0x06, 0x01, (byte) 0xBF}; //自动测量  A50601BF
    static final byte[] ORDER_DATA_OUTPUT_MANUAL = new byte[]{(byte) 0xA5, 0x06, 0x00, (byte) 0xBF}; //手动测量  A50600BF

    public SMLX90621_YS() {
        super(DEFAULT_DEVICE, DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 50, 100, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
//        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

//    @Override
//    public TakeTempEntity[] getDefaultTakeTempEntities() {
//        TakeTempEntity[] entities = new TakeTempEntity[1];
//        TakeTempEntity entity3 = new TakeTempEntity();
//        entity3.setDistances(30);
//        entity3.setTakeTemperature(6.16f);
//        entities[0] = entity3;
//        return entities;
//    }

    @Override
    public byte[] getOrderDataOutputType(boolean isAuto) {
        return isAuto ? ORDER_DATA_OUTPUT_AUTO : ORDER_DATA_OUTPUT_MANUAL;
    }

    @Override
    public byte[] getOrderDataOutputQuery() {
        return ORDER_DATA_OUTPUT_MANUAL;
    }

    @Override
    public boolean isPoint() {
        return false;
    }

    @Override
    public byte[] oneFrame(byte[] data) {
        return data;
    }

    int count = 0;
    List<Float> mFloats = new ArrayList<>();
    float lastTemp = 0;
    int tempCount = 0;

//    @Override
//    public float check(float value, float ta) {
//        TakeTempEntity takeTempEntity = getTakeTempEntity();
//        if (!takeTempEntity.isNeedCheck()) return value;
//        count++;
//        mFloats.add(value);
//        if (mFloats.size() == 6) {
//            tempCount = 5;
//        } else if (mFloats.size() > 6) {
//            List<Float> floats = mFloats.subList(tempCount - 3, tempCount - 3 + 5);
//            float sum = 0;
//            float max = floats.get(0);
//            float min = floats.get(0);
//
//            for (int i = 0; i < floats.size(); i++) {
//                sum += floats.get(i);
//                if (floats.get(i) > max) max = floats.get(i);
//                if (floats.get(i) < min) min = floats.get(i);
//            }
//
//            float tt = sum / 5f + takeTempEntity.getTakeTemperature();
//            if (tt >= 34f && tt < 36f) {
//                int tt1 = (int) (tt * 100);
//                tt = Float.parseFloat("36." + String.valueOf(tt1).substring(2, 4));
//            } else if (tt >= 37.2f && tt <= 37.5f) {
//                tt += 0.3f;
//            }
//            getStorager().add(tempCount + ":" + floats + " t:" + tt);
//            lastTemp = tt;
//            tempCount++;
//            return tt;
//        }
//        return lastTemp;
//    }

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null) return null;
        if ((data[0] & 0xFF) == 0xa5 && (data[1] & 0xFF) == 0x06
                && (data[data.length - 1] & 0xFF) == 0xBF) {
            TemperatureEntity entity = new TemperatureEntity();
            List<Float> temps = new ArrayList<>();
            entity.min = entity.max = (((data[3] & 0xFF) << 8 | (data[4] & 0xFF))) / 100.0f;
            entity.ta = ((data[data.length - 5] & 0xFF) << 8 | (data[data.length - 4] & 0xFF)) / 100.0f;
            for (int i = 3; i < data.length - 5; i = i + 2) {
                int sum = (data[i] & 0xFF) << 8 | (data[i + 1] & 0xFF);
                float temp = sum / 100f;
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
