package com.ys.temperaturelib.device;

import android.os.SystemClock;
import android.text.TextUtils;

import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.utils.FileUtil;

import java.io.File;


public class IPointThermometer extends MeasureDevice {
    File tempFile;
    boolean enabled;
    DataRead mDataRead;
    TemperatureStorager mStorager;
    String filePath;

    public IPointThermometer() {
        mStorager = new TemperatureStorager();
    }

    public IPointThermometer(String filePath) {
        this();
        this.filePath = filePath;
    }

    public TemperatureStorager getStorager() {
        return mStorager;
    }

    @Override
    protected boolean init() {
        if (filePath == null) return false;
        tempFile = new File(filePath);
        enabled = tempFile.exists() && tempFile.canRead();
        return enabled;
    }

    /**
     * 获取单点温度
     *
     * @return
     */
    public TemperatureEntity getTemperature() {
        if (!enabled) return null;
        String readFile = FileUtil.readFile(tempFile);
        if (!TextUtils.isEmpty(readFile)) {
            String[] split = readFile.split(" ");
            TemperatureEntity entity = new TemperatureEntity();
            entity.ta = Integer.parseInt(split[1]) / 100f;
            entity.temperatue = check(Integer.parseInt(split[0]) / 100f, entity.ta);
            return entity;
        }
        return null;
    }

    public void startUp(MeasureResult result) {
        startUp(result, getParm().perid);
    }

    @Override
    public void startUp(MeasureResult result, long period) {
        enabled = init();
        if (enabled) {
            if (mDataRead != null) mDataRead.interrupt();
            mDataRead = null;
            mDataRead = new DataRead(result, period);
            mDataRead.start();
        }
    }

    @Override
    public void order(byte[] data) {

    }

    @Override
    public void destroy() {
        if (mDataRead != null) mDataRead.interrupt();
        mDataRead = null;
        if (mStorager != null)
            mStorager.exit();
        mStorager = null;
    }

    @Override
    public float check(float value, float ta) {
        return value;
    }

    public boolean isPoint() {
        return true;
    }

    /**
     * 数据读取线程
     */
    class DataRead extends Thread {
        MeasureResult mResult;
        long period;
        boolean isInterrupted;

        public DataRead(MeasureResult result, long period) {
            mResult = result;
            this.period = period;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            isInterrupted = true;
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                SystemClock.sleep(period);
                if (isInterrupted) break;
                TemperatureEntity entity = getTemperature();
                if (mResult != null && entity != null) mResult.onResult(entity, null);
                if (mStorager != null) mStorager.add(entity);
            }
        }
    }
}
