package com.ys.temperaturelib.utils;

import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class TemperatureSerialPortHelper {

    private static final String TAG = "TSerialPortHelper";

    //模式1，热成像模式
    public static final int MODE_IMAGE = 1;
    //模式二，全阵列模式
    public static final int MODE_FULL_ARRAY = 2;

    //是否主动模式
    private boolean isActiveMode = false;

    //主动模式下，缓存的数据数量
    int bufferCount;

    private int currentMode = MODE_IMAGE;

    ParseHelper parseHelper;


    SerialPort serialPort;
    OutputStream outputStream;

    InputStream inputStream;
    Thread readThread;
    boolean readFlag;


    public TemperatureSerialPortHelper(int currentMode, boolean isAcitiveMode, int bufferCount) {
        this.currentMode = currentMode;
        this.isActiveMode = isAcitiveMode;
        this.bufferCount = bufferCount;

        init();
    }


    public void start() {
        readData();
    }


    public void stop() {
        readFlag = false;
        if (readThread != null) {
            try {
                readThread.interrupt();
                readThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (serialPort != null) {
            serialPort.close();
        }

    }


    private void init() {
        parseHelper = new ParseHelper();

        if (currentMode == MODE_IMAGE) {
            parseHelper.setDataLenth(2071);
        } else if (currentMode == MODE_FULL_ARRAY) {
            parseHelper.setDataLenth(2061);
        }
        parseHelper.setBufferCount(bufferCount);

        try {
            serialPort = new SerialPort(new File("/dev/ttyS3"), 921600, 0);
            outputStream = serialPort.getOutputStream();
            inputStream = serialPort.getInputStream();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //被动模式下，主从获取温度
    public void getTem() {
        sendData(" FF FF FE 25 00 00 FE FF FF");//被动获取
    }


    public byte[] readData() {
        final byte[][] datas = {null};
        if (serialPort == null) return null;
        if (inputStream == null) return null;
        if (readThread == null) {
            readFlag = true;
            readThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    SystemClock.sleep(200);
                    if (currentMode == MODE_IMAGE) {
                        sendData("FF FF FE 29 00 00 FE FF FF");//切换红外热像
                    } else if (currentMode == MODE_FULL_ARRAY) {
                        sendData("FF FF FE 29 00 01 FE FF FF");//切换为全阵列，不输出红外热像
                    }

                    SystemClock.sleep(100);
                    sendData("ff ff fe 2b 00 01 fe ff ff");//打开帧头

                    SystemClock.sleep(100);
                    if (isActiveMode) {
                        sendData("ff ff fe 28 00 01 fe ff ff");//主动上报
                    } else {
                        sendData("ff ff fe 28 00 00 fe ff ff");//被动上报
                    }


                    while (readFlag) {
                        try {
                            if (inputStream.available() > 0) {

                                //   Thread.sleep(100);

                                byte[] buffer = new byte[inputStream.available()];
                                Log.d("sky", "int = " + inputStream.available());
                                int size = inputStream.read(buffer);
                                datas[0] = parseHelper.parseContent(buffer);


                                //  Log.i(TAG, "readdata:" + Utils.byte2HexStr(buffer));
                                //  Log.i(TAG, "readdata");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            readThread.start();
        }
        return datas[0];
    }


    private void sendData(String commond) {
        commond = commond.replace(" ", "");
        Log.i(TAG, "sendData:" + commond);
        byte[] bytes = DataFormatUtil.toBytes(commond);
        writeData(bytes);
    }

    private void writeData(byte[] data) {
        if (serialPort == null) return;
        if (outputStream == null) return;
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


