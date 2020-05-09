package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class CHUWO_32x32 extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "触沃32*32(矩阵)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 115200; //波特率

    static final int MATRIX_COUT_X = 32; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 32; //温度矩阵纵坐标总数量

    static final byte[] ORDER_DATA_OUTPUT = new byte[]{(byte) 0xEE, (byte) 0xE1, 0x01, 0x55,
            (byte) 0xFF, (byte) 0xFC, (byte) 0xFD, (byte) 0xFF}; //查询输出数据指令  0xEE +0xE1+0x01+0x55+0xFF+0xFC+0xFD+0xFF

    byte[] oneFrameData = new byte[2055];
    int index;

    public CHUWO_32x32() {
        super(DEFAULT_DEVICE, DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 24, 300, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
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
        if (data.length != oneFrameData.length) return null;
        if ((data[0] & 0xFF) == 0xE1) {
            System.arraycopy(data, 0, oneFrameData, 0, data.length);
            index = data.length;
            if (index == oneFrameData.length) return oneFrameData;
        } else {
            if (index + data.length == oneFrameData.length) {
                System.arraycopy(data, 0, oneFrameData, index, data.length);
                index = 0;
                return oneFrameData;
            }
        }
        return null;
    }

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null) return null;
        TemperatureEntity entity = new TemperatureEntity();
        List<Float> temps = new ArrayList<>();
        entity.ta = (((data[2049] & 0xFF) << 8
                | (data[2050] & 0xFF)) - 2731) / 10.0f;
        entity.min = entity.max = (((data[1] & 0xFF) << 8 | (data[2] & 0xFF)) - 2731) / 10.0f;
        float temp = 0f;
        for (int i = 0; i < data.length - 7; i = i + 2) {
            int sum = (data[i + 1] & 0xFF) << 8 | (data[i + 2] & 0xFF);
            temp = check((sum - 2731) / 10.0f,entity.ta);
            if (temp < entity.min) entity.min = temp;
            if (temp > entity.max) entity.max = temp;
            temps.add(temp);
        }
        entity.tempList = temps;
        entity.temperatue = temp;
        return entity;
    }
}
