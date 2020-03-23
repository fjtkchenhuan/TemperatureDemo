package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SHAIMAN_32x24 extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "HAIMAN-32*24(矩阵)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 115200; //波特率

    static final int MATRIX_COUT_X = 32; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 24; //温度矩阵纵坐标总数量

    static final byte[] ORDER_DATA_OUTPUT = new byte[]{(byte) 0xA5, 0x55, 0x01, (byte) 0xFB}; //查询输出数据指令

    TemperatureEntity entity = new TemperatureEntity();
    float[] temps = new float[768];
    List<Float> tempList;

    public SHAIMAN_32x24() {
        super(DEFAULT_DEVICE, DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 40, 0, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
        Arrays.fill(temps, 20f);
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[3];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(-0.16f);
        entities[0] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(-0.09f);
        entities[1] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(50);
        entity3.setTakeTemperature(0.48f);
        entities[2] = entity3;

        return entities;
    }

    @Override
    public byte[] getOrderDataOutputType(boolean isAuto) {
        return ORDER_DATA_OUTPUT;
    }

    @Override
    public byte[] getOrderDataOutputQuery() {
        return ORDER_DATA_OUTPUT;
    }

    @Override
    public boolean isPoint() {
        return false;
    }

    @Override
    public byte[] oneFrame(byte[] data) {
        if ((data[2] & 0xFF) == 0x00 && (data[3] & 0xFF) == 0x00) {
            return null;
        }
        return data;
    }

    @Override
    public float check(float value,float ta) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        return value + takeTempEntity.getTakeTemperature();
    }

    long count;

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null) return null;
        int x = data[4] & 0xFF;
        int y = data[5] & 0xFF;
        entity.temperatue = ((data[2] & 0xFF) + 256 * (data[3] & 0xFF)) / 100f;
        entity.temperatue = check(entity.temperatue,0);
        int index = y * 32 + x;
        temps[index] = entity.temperatue;
        count++;
        if (count % 5 == 0) {
            tempList = new ArrayList<>();
            for (int i = 0; i < temps.length; i++) {
                tempList.add(temps[i]);
            }
            entity.tempList = tempList;
            return entity;
        }
        return null;
    }
}
