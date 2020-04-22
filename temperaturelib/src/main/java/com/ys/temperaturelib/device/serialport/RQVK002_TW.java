package com.ys.temperaturelib.device.serialport;

import android.util.Log;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

public class SGY_MCU90614_TW extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "GY-MCU90614-TW(单点)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS0"; //设备号
    static final int DEFAULT_RATE = 9600; //波特率

    static final byte[] ORDER_DATA_OUTPUT_QUERY = new byte[]{(byte) 0xA5, 0x15, (byte) 0xBA};//查询输出数据指令
    static final byte[] ORDER_DATA_OUTPUT_AUTO = new byte[]{(byte) 0xA5, 0x45, (byte) 0xEA}; //自动输出数据指令

    static final byte[] ORDER_BAUDRATE_9600 = new byte[]{(byte) 0xA5, (byte) 0xAE, 0x53};//波特率9600 设置指令
    static final byte[] ORDER_BAUDRATE_115200 = new byte[]{(byte) 0xA5, (byte) 0xAF, 0x54};//波特率115200 设置指令

    static final byte[] ORDER_POWERUP_AUTO = new byte[]{(byte) 0xA5, 0x51, (byte) 0xF6};//上电后自动输出温度数据(默认)
    static final byte[] ORDER_POWERUP = new byte[]{(byte) 0xA5, 0x52, (byte) 0xF7};//上电后不自动输出温度数据

    public SGY_MCU90614_TW() {
        super(DEFAULT_DEVICE, DEFAULT_RATE
                , new MeasureParm(DEFAULT_MODE_NAME, 0, 500, 0, 0));
        setTemperatureParser(this);
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[3];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(0.89f);
        entities[0] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(1.36f);
        entities[1] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(50);
        entity3.setTakeTemperature(2.41f);
        entities[2] = entity3;

        return entities;
    }

    @Override
    public byte[] getOrderDataOutputType(boolean isAuto) {
        return new byte[0];
    }

    @Override
    public byte[] getOrderDataOutputQuery() {
        return new byte[0];
    }

    @Override
    public boolean isPoint() {
        return true;
    }

    @Override
    public float check(float value,float ta) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        return value + takeTempEntity.getTakeTemperature();
    }

    @Override
    public byte[] oneFrame(byte[] data) {
        byte[] temp = null;
        if ((data[0] & 0xFF) == 0x5A
                && (data[1] & 0xFF) == 0x5A) {
            temp = new byte[10];
            byte check = 0x00;
            for (int i = 0; i < 8; i++) {
                check = (byte) (check + (data[i] & 0xFF));
            }
            if (check == data[8]) {
                System.arraycopy(data, 0, temp, 0, 10);
            }
        }
        return temp;
    }

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null) return null;
        TemperatureEntity entity = new TemperatureEntity();
        entity.ta = ((data[6] & 0xFF) << 8 | (data[7] & 0xFF)) / 100f;
        entity.temperatue = ((data[4] & 0xFF) << 8 | (data[5] & 0xFF)) / 100f;
        entity.temperatue = check(entity.temperatue,entity.ta);
        return entity;
    }
}
