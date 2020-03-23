package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class SGY_MCU90640_32x24 extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "GY-MCU90640-32*24(矩阵)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 115200; //波特率

    static final int MATRIX_COUT_X = 32; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 24; //温度矩阵纵坐标总数量

    static final byte[] ORDER_BAUDRATE_9600 = new byte[]{(byte) 0xA5, 0x15, 0x01, (byte) 0xBB};//波特率9600 设置指令
    static final byte[] ORDER_BAUDRATE_115200 = new byte[]{(byte) 0xA5, 0x15, 0x02, (byte) 0xBC};//波特率115200 设置指令
    static final byte[] ORDER_BAUDRATE_460800 = new byte[]{(byte) 0xA5, 0x15, 0x03, (byte) 0xBD};//波特率460800 设置指令

    static final byte[] ORDER_UPDATERATE_0_5 = new byte[]{(byte) 0xA5, 0x25, 0x00, (byte) 0xCA};//模块更新频率0.5hz 设置指令
    static final byte[] ORDER_UPDATERATE_1 = new byte[]{(byte) 0xA5, 0x25, 0x02, (byte) 0xCB};//模块更新频率1hz 设置指令
    static final byte[] ORDER_UPDATERATE_2 = new byte[]{(byte) 0xA5, 0x25, 0x03, (byte) 0xCC};//模块更新频率2hz 设置指令
    static final byte[] ORDER_UPDATERATE_4 = new byte[]{(byte) 0xA5, 0x25, 0x04, (byte) 0xCD};//模块更新频率3hz 设置指令
    static final byte[] ORDER_UPDATERATE_8 = new byte[]{(byte) 0xA5, 0x25, 0x05, (byte) 0xCE};//模块更新频率4hz 设置指令

    static final byte[] ORDER_DATA_OUTPUT_QUERY = new byte[]{(byte) 0xA5, 0x35, 0x01, (byte) 0xDB};//查询输出数据指令
    static final byte[] ORDER_DATA_OUTPUT_AUTO = new byte[]{(byte) 0xA5, 0x35, 0x02, (byte) 0xDC}; //自动输出数据指令

    byte[] oneFrameData = new byte[1544];
    int index;

    public SGY_MCU90640_32x24() {
        super(DEFAULT_DEVICE, DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 35, 150, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
    }

    @Override
    public byte[] getOrderDataOutputType(boolean isAuto) {
        return isAuto ? ORDER_DATA_OUTPUT_AUTO : ORDER_DATA_OUTPUT_QUERY;
    }

    @Override
    public byte[] getOrderDataOutputQuery() {
        return ORDER_DATA_OUTPUT_QUERY;
    }

    @Override
    public boolean isPoint() {
        return false;
    }


    @Override
    public byte[] oneFrame(byte[] data) {
        if ((data[0] & 0xFF) == 0x5A
                && (data[1] & 0xFF) == 0x5A) {
            System.arraycopy(data, 0, oneFrameData, 0, data.length);
            index = data.length;
            if (index == 1544) return oneFrameData;
        } else {
            if (index + data.length == 1544) {
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
        int count = data[3] * 256 + data[2];
        entity.ta = ((data[1541] & 0xFF) * 256 + (data[1540] & 0xFF)) / 100f;
        entity.min = entity.max = ((data[5] & 0xFF) * 256 + (data[4] & 0xFF)) / 100f;
        for (int i = 0; i < count; i = i + 2) {
            int sum = ((data[i + 5] & 0xFF) * 256 + (data[i + 4] & 0xFF));
            float temp = check(sum / 100f,entity.ta);
            if (temp < entity.min) entity.min = temp;
            if (temp > entity.max) entity.max = temp;
            temps.add(temp);
        }
        entity.tempList = temps;
        entity.temperatue = entity.max;
        return entity;
    }
}
