package com.ys.temperaturelib.device;

import android.os.SystemClock;

import com.ys.serialport.SerialPort;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;


/**
 * 串口设备
 */
public class SThermometer extends MeasureDevice {
    public static String[] RATES = new String[]{"0", "50", "75", "110", "134", "150", "200", "300",
            "600", "1200", "1800", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400",
            "460800", "500000", "576000", "921600", "1000000", "1152000", "1500000", "2000000",
            "2500000", "3000000", "3500000", "4000000"};
    public static String[] DEVICES = new String[]{"dev/ttyS0","dev/ttyS1","dev/ttyS2","dev/ttyS3",
            "dev/ttyS4","dev/ttyGS0","dev/ttyGS1","dev/ttyGS2","dev/ttyGS3","dev/ttyFIQ0"};
    public static String[] getDevices() {
//        String result = FileUtil.exec("find /dev/ -name \"tty*\"");
//        if (TextUtils.isEmpty(result)) return null;
//        return result.split(" ");
        return DEVICES;
    }

    SerialPort mSerialPort;
    boolean mEnabled;
    DataRead mDataRead;
    String mDevice;
    int mBaudrate;
    TemperatureParser<byte[]> mParser;
    TemperatureStorager mStorager;
    boolean isWriteInThread = false;
    byte[] mOrder;

    public SThermometer() {
        mStorager = new TemperatureStorager();
    }
    public TemperatureStorager getStorager(){
        return mStorager;
    }
    public void setTemperatureParser(TemperatureParser<byte[]> parser) {
        mParser = parser;
    }

    @Override
    protected boolean init() {
        if (mSerialPort != null) mSerialPort.release();
        mSerialPort = null;
        mSerialPort = new SerialPort();
        return mSerialPort.init(mDevice, mBaudrate);
    }

    public void setDevice(String device) {
        mDevice = device;
    }

    public void setBaudrate(int baudrate) {
        mBaudrate = baudrate;
    }

    public String getDevice() {
        return mDevice;
    }

    public int getBaudrate() {
        return mBaudrate;
    }

    @Override
    public void startUp(MeasureResult result, long period) {
        if (mDataRead != null) mDataRead.interrupt();
        mDataRead = null;
        mEnabled = init();
        if (mEnabled) {
            mDataRead = new DataRead(result, period);
            mDataRead.start();
        }
    }

    @Override
    public void order(byte[] data) {
        if (mEnabled && mSerialPort != null) {
            mSerialPort.write(data);
        }
        if (isWriteInThread)
            mOrder = data;
    }

    /**
     * 设置在子线程中写命令
     */
    public void setWriteInThread(boolean isWriteInThread) {
        this.isWriteInThread = isWriteInThread;
    }

    @Override
    public void destroy() {
        if (mDataRead != null) mDataRead.interrupt();
        mDataRead = null;
        mStorager.exit();
        mStorager = null;
    }

    @Override
    public float check(float value,float ta) {
        return value;
    }

    @Override
    public boolean isPoint() {
        return false;
    }

    /**
     * 串口数据读取线程
     */
    class DataRead extends Thread {
        long period;
        MeasureResult result;
        boolean isInterrupted;

        public DataRead(MeasureResult result, long period) {
            this.result = result;
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
                if (mSerialPort != null) {
                    if (isWriteInThread && mOrder != null) {
                        mSerialPort.write(mOrder);
                        SystemClock.sleep(20);
                    }
                    byte[] read = mSerialPort.read(period);
                    if (!isInterrupted && read != null && mParser != null) {
                        byte[] oneFrame = mParser.oneFrame(read);
                        if (oneFrame != null) {
                            TemperatureEntity entity = mParser.parse(oneFrame);
                            if (result != null && entity != null) result.onResult(entity, oneFrame);
                            if (mStorager != null) mStorager.add(entity);
                        }
                    }
                }
                if (isInterrupted) {
                    break;
                }
                SystemClock.sleep(period);
            }
        }
    }
}
