package com.ys.temperaturelib.device;

import android.os.Environment;
import android.os.SystemClock;

import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.utils.FileUtil;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class TemperatureStorager implements Runnable {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/YsTemperature.txt";
    Queue<TemperatureEntity> mQueue = new LinkedList<>();
    boolean isWorked = true;
    Thread mThread;
    DecimalFormat fnum = new DecimalFormat("##0.00");
    Queue<String> mQueue1 = new LinkedList<>();

    public void add(String temp) {
        mQueue1.add(temp);

    }

    public void add(TemperatureEntity entity) {
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
        }
        if (entity != null)
            mQueue.add(entity);
    }

    public void exit() {
        isWorked = false;
        if (mThread != null)
            mThread.interrupt();
        mThread = null;
    }

    @Override
    public void run() {
        FileUtil.writeFileAppend(fileName, new String("\n\n\n"));
        while (isWorked) {
            TemperatureEntity entity = mQueue.poll();
            String poll = mQueue1.poll();
            if (entity != null) {
                StringBuffer mBuffer = new StringBuffer();
                mBuffer.append(simpleDateFormat.format(new Date(System.currentTimeMillis())));
                mBuffer.append(":");
//                mBuffer.append(" MIN=" + fnum.format(entity.min));
//                mBuffer.append(" MAX=" + fnum.format(entity.max));
                mBuffer.append("TA1=" + fnum.format(entity.ta));
                mBuffer.append("TO1=" + fnum.format(entity.temperatue));
                mBuffer.append("\n");
                if(entity.tempList != null && entity.tempList.size() >= 6){
//                    mBuffer.append(" List=" + entity.tempList.subList(0, 6));
//                    mBuffer.append("\n");
                }
                if(poll != null){
                    mBuffer.append("TT1:" + poll);
                    mBuffer.append("\n");
                }
                FileUtil.writeFileAppend(fileName, mBuffer.toString());
            }
            SystemClock.sleep(100);
        }
    }
}
