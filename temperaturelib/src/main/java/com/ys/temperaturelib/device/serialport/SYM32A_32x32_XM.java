package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class SYM32A_32x32_XM extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "YM32A-32*32-XM(矩阵)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 921600; //波特率

    static final int MATRIX_COUT_X = 32; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 32; //温度矩阵纵坐标总数量

    static final byte[] ORDER_DATA_OUTPUT_AUTO_CLOSE = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x28, 0x00, 0x00,
            (byte) 0xFE, (byte) 0xFF, (byte) 0xFF};//关闭数据主动输出
    static final byte[] ORDER_DATA_OUTPUT_AUTO_OPEN = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x28, 0x00, 0x01,
            (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}; //开启数据主动输出

    static final byte[] ORDER_DATA_OUTPUT_TYPE_1 = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x29, 0x00, 0x00,
            (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}; //切换主动传输数据类型:热像+极值温度
    static final byte[] ORDER_DATA_OUTPUT_TYPE_2 = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x29, 0x00, 0x01,
            (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}; //切换主动传输数据类型:热像+全阵列温度

    static final byte[] ORDER_DATA_OUTPUT_FORMAT_16 = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x26, 0x00, 0x00,
            (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}; //温度数据输出格式:16位 该命令只有在系统要求输出全阵列温度数据的时候才有用
    static final byte[] ORDER_DATA_OUTPUT_FORMAT_8 = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x26, 0x00, 0x01,
            (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}; //温度数据输出格式:8位 该命令只有在系统要求输出全阵列温度数据的时候才有用
    static final byte[] ORDER_DATA_OUTPUT = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFE, 0x25, 0x00, 0x00,
            (byte) 0xFE, (byte) 0xFF, (byte) 0xFF}; //上报最近一次温度或者热像数据 该命令只有在关闭数据主动传输的时候才可以使用

    byte[] oneFrameData = new byte[2061];
    int index;

    public SYM32A_32x32_XM() {
        super(DEFAULT_DEVICE,DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 35, 150, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
    }

    public byte[][] getInitOrder() {
        return new byte[][]{ORDER_DATA_OUTPUT_TYPE_2, ORDER_DATA_OUTPUT_FORMAT_16, ORDER_DATA_OUTPUT_AUTO_CLOSE};
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
    public byte[] getOrderDataOutputType(boolean isAuto) {
        return isAuto ? ORDER_DATA_OUTPUT_AUTO_OPEN : ORDER_DATA_OUTPUT_AUTO_CLOSE;
    }

    byte[] lastData;

    @Override
    public byte[] oneFrame(byte[] data) {
        if (lastData == null) {
            boolean isOk = (data[0] & 0xFF) == 0x41 && (data[1] & 0xFF) == 0x42 && (data[2] & 0xFF) == 0x43
                    && (data[3] & 0xFF) == 0x44 && (data[4] & 0xFF) == 0x45 && (data[5] & 0xFF) == 0x46;
            if (!isOk) return null;
            lastData = new byte[data.length];
            System.arraycopy(data, 0, lastData, 0, data.length);
        } else {
            byte[] temp = new byte[lastData.length + data.length];
            System.arraycopy(lastData, 0, temp, 0, lastData.length);
            System.arraycopy(data, 0, temp, lastData.length, data.length);
            if (temp.length == oneFrameData.length) {
                lastData = null;
                return temp;
            } else if (temp.length > oneFrameData.length) {
                byte[] temp1 = new byte[oneFrameData.length];
                System.arraycopy(temp, 0, temp1, 0, oneFrameData.length);
                lastData = new byte[temp.length - oneFrameData.length];
                System.arraycopy(temp, oneFrameData.length, lastData, 0,
                        temp.length - oneFrameData.length);
                return temp1;
            }
        }
        return null;
    }

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null) return null;
        TemperatureEntity entity = new TemperatureEntity();
        List<Float> temps = new ArrayList<>();
        entity.min = entity.max = ((data[6] & 0xFF) << 8 | (data[7] & 0xFF)) / 10.0f;
        for (int i = 5; i < data.length - 9; i = i + 2) {
            int sum = (data[i + 1] & 0xFF) << 8 | (data[i + 2] & 0xFF);
            float temp = check(sum / 10.0f,0);
            if (temp < entity.min) entity.min = temp;
            if (temp > entity.max) entity.max = temp;
            temps.add(temp);
        }
        entity.tempList = temps;
        entity.temperatue = entity.max;
        return entity;
    }
}
