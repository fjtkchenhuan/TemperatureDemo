package com.ys.serialport;


import android.os.SystemClock;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SerialPort {

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;


    /**
     * 初始化串口
     *
     * @param device  设备号
     * @param baudrat 波特率
     * @return 成功返回true 失败返回false
     */
    public boolean init(String device, int baudrat) {
        File file = new File(device);
        if (!file.canRead() || !file.canWrite()) {
            try {
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 777 " + file.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !file.canRead() || !file.canWrite()) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mFd = open(file.getAbsolutePath(), baudrat);
        if (mFd == null) {
            return false;
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
        return true;
    }

    /**
     * 写串口指令
     *
     * @param data
     * @return
     */
    public boolean write(byte[] data) {
        if (data == null || mFileOutputStream == null) return false;
        try {
            mFileOutputStream.write(data);
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取串口数据
     *
     * @return
     */
    public byte[] read(long sleepms) {
        if (mFileInputStream == null) return null;
        try {
            if (mFileInputStream.available() > 0) {
                SystemClock.sleep(sleepms);
                if (mFileInputStream == null) return null;
                byte[] buffer = new byte[mFileInputStream.available()];
                mFileInputStream.read(buffer);
                return buffer;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 释放串口
     */
    public void release() {
        try {
            if (mFileOutputStream != null) {
                mFileOutputStream.close();
                mFileOutputStream = null;
            }
            if (mFileInputStream != null) {
                mFileInputStream.close();
                mFileInputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mFd != null) {
            close();
        }
    }

    static {
        System.loadLibrary("serial_port");
    }

    public native FileDescriptor open(String path, int baudrate);

    public native void close();
}
