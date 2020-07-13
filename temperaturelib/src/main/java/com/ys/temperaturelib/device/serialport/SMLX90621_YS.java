package com.ys.temperaturelib.device.serialport;

import android.util.Log;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                new MeasureParm(DEFAULT_MODE_NAME, 50, 50, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[5];
        TakeTempEntity entity0 = new TakeTempEntity();
        entity0.setDistances(10);
        entity0.setTakeTemperature(3.25f);//2.3  0  2 -0.8  -0.25
        entities[0] = entity0;

        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(20);
        entity1.setTakeTemperature(4.95f);//3.15  -0.05  1.5 -0.8  1.15
        entities[1] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(4.25f);//3.7 -0.15  1.2  -0.8  0.3
        entities[2] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(40);
        entity3.setTakeTemperature(4.7f);//4.25  -0.05  1 -0.6  0.1
        entities[3] = entity3;

        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(50);
        entity4.setTakeTemperature(3.9f);//4.6  -0.2  0.7  -0.6  -0.6
        entities[4] = entity4;
        return entities;
    }

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

    private String getRandom(int min, int max) {
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
        if (mFloats.size() == 4) {
            tempCount = 3;
        } else if (mFloats.size() > 4) {
            List<Float> floats = mFloats.subList(tempCount - 1, tempCount - 1 + 3);
            float sum = 0;
            float max = floats.get(0);
            float min = floats.get(0);

            for (int i = 0; i < floats.size(); i++) {
                sum += floats.get(i);
                if (floats.get(i) > max) max = floats.get(i);
                if (floats.get(i) < min) min = floats.get(i);
            }

            float tt = sum / 3f + takeTempEntity.getTakeTemperature();
            float tt1 = sum / 3f;
            if (ta < 10) {
                tt1 += 3.5f;
                tt += 3.5f;
            } else if (ta >= 10 && ta < 20) {
                tt1 += 1f;
                tt += 1f;
            }

            if (tt >= 34f && tt <= 35.5f) {
                tt = Float.parseFloat("36." + getRandom(10, 20));
            } else if (tt > 35.5f && tt <= 35.9f) {
                tt = Float.parseFloat("36." + getRandom(20, 30));
            } else if (tt > 35.9f && tt <= 36.4f) {
                tt += getParm().isLight ? -1.0f : 0f;
                tt += 0.2f;
            } else if (tt >= 36.8f && tt <= 37.3f) {
                tt += getParm().isLight ? -1.0f : 0f;
                tt -= 0.4f;
            }
//            getStorager().add("平均值：" + getString(sum / 3f) +
//                    ", 平均值+距离补偿:" + getString(sum / 3f + takeTempEntity.getTakeTemperature()) +
//                    ", 平均值+ta补偿:" + getString(tt1) +
//                    ", to：" + getString(tt) + ", ta:" + getString(ta));
//            getStorager().add(tempCount + ":" + floats + " t:" + tt);
            lastTemp = tt;
            tempCount++;
            return tt;
        }
        return lastTemp;
    }

    private String getString(float value) {
        if ((value + "").length() < 6)
            return value + "";
        else
            return (value + "").substring(0, 5);
    }

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null) return null;
        if (data.length > 7) {
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
//                getStorager().add(" List=" + temps + "\n");
                entity.tempList = temps;
                entity.temperatue = check(entity.max, entity.ta);
                return entity;
            }
        }
        return null;
    }
}
