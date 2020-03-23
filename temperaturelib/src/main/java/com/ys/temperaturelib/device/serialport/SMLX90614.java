package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class SMLX90614 extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "MLX90614(单点)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS0"; //设备号
    static final int DEFAULT_RATE = 9600; //波特率

    public SMLX90614() {
        super(DEFAULT_DEVICE,DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME,0,500,0,0));
        setTemperatureParser(this);
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
    public byte[] oneFrame(byte[] data) {
        return data;
    }

    @Override
    public TemperatureEntity parse(byte[] data) {
        try {
            //一帧数据格式为ASCII，可根据ASCII码对照表解析
            //以下只解析温度测量数据，循环字节数组当数据下标i为12/32(ASCII对应小数点'.')时
            //选取i-2为十位、i-1为个位、i+1为小数点后一位、i+2为小数点后二位
            //解析后的温度值有两个工作温度、测量温度。这里返回测量温度
            List<Float> temps = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                if (i == 12 || i == 32) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append((data[i - 2] & 0xFF) - 48);
                    buffer.append((data[i - 1] & 0xFF) - 48);
                    buffer.append(".");
                    buffer.append((data[i + 1] & 0xFF) - 48);
                    buffer.append((data[i + 2] & 0xFF) - 48);
                    temps.add(Float.parseFloat(buffer.toString()));
                }
            }
            if (temps.size() > 1) {
                TemperatureEntity entity = new TemperatureEntity();
                entity.ta = temps.get(0);
                entity.temperatue = check(temps.get(1),entity.ta);
                return entity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
