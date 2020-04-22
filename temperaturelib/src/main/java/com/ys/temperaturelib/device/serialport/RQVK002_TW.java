package com.ys.temperaturelib.device.serialport;

import android.util.Log;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;
import com.ys.temperaturelib.utils.DataFormatUtil;

import java.util.Arrays;

public class RQVK002_TW extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "RQVK002-TW(单点)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 19200; //波特率

    public RQVK002_TW() {
        super(DEFAULT_DEVICE, DEFAULT_RATE
                , new MeasureParm(DEFAULT_MODE_NAME, 0, 500, 0, 0));
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
    public float check(float value, float ta) {
        return value;
    }

    @Override
    public byte[] oneFrame(byte[] data) {
        byte[] temp;
        if (data.length == 6 && (data[0] & 0xFF) == 0x5A)
            return data;
        else {
            temp = new byte[6];
            if ((data[0] & 0xFF) == 0x5A)
                System.arraycopy(data, 0, temp, 0, 6);
        }
        return temp;
    }

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null) return null;
        TemperatureEntity entity = new TemperatureEntity();
        if (data.length == 6 && (data[0] & 0xFF) == 0x5A && (data[5] & 0xFF) == 0xA5) {
            int tempTa = data[2] & 0xFF;
            if (tempTa > 127)
                entity.ta = (tempTa - 255) / 10f + 25.0f;
            else
                entity.ta = tempTa / 10f + 25.0f;

            int tempTo = data[3] & 0xFF;
            if (tempTo > 127)
                entity.temperatue = (tempTo - 255) / 10f + 36.0f;
            else
                entity.temperatue = (tempTo) / 10f + 36.0f;
        }
        return entity;
    }
}
