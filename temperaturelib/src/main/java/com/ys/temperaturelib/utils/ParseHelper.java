package com.ys.temperaturelib.utils;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;

public class ParseHelper {
    private static final String TAG = "TSerialPortHelper";
    int bufferCount;
    LimitedQueue<Float> queue;
    //获取平均值的时间
    long getAverageTime = 0;
    //加载温度的时间
    long addTemTime = 0;
    private int dataLenth;
    private int mBufferSize;
    private byte[] mBuffer;
    private int mCurIndex = 0;

    public void setDataLenth(int dataLenth) {
        this.dataLenth = dataLenth;
        mBufferSize = dataLenth * 10;
        mBuffer = new byte[mBufferSize];
    }

    public int getBufferCount() {
        return bufferCount;
    }

    public void setBufferCount(int bufferCount) {
        this.bufferCount = bufferCount;
        queue = new LimitedQueue<>(bufferCount);
    }


    public LimitedQueue<Float> getQueue() {
        return queue;
    }

    public void setQueue(LimitedQueue<Float> queue) {
        this.queue = queue;
    }

    public byte[] parseContent(final byte[] buffer) {
        if (buffer != null && buffer.length > 0) {
            for (int i = 0; i < buffer.length; i++) {
                mBuffer[mCurIndex] = buffer[i];
                mCurIndex++;
            }
            int cacheLength = dataLenth;
            if (mCurIndex >= cacheLength) {
                int endIndex = findEndIndex();
                if (endIndex != -1)
                    return pickData(endIndex);
            }
            if (mCurIndex > mBufferSize * 4 / 5)
                mCurIndex = 0;
        }
        return null;
    }

    private int findEndIndex() {
        int endIndex = -1;
        for (int i = mCurIndex - 6; i > 0; i--) {
            if (mBuffer[i] == 85 && mBuffer[i + 1] == 86
                    && mBuffer[i + 2] == 87 && mBuffer[i + 3] == 88
                    && mBuffer[i + 4] == 89 && mBuffer[i + 5] == 90) {
                endIndex = i + 6;
                break;
            }
        }
        return endIndex;
    }

    private byte[] pickData(int endIndex) {
        byte[] buf = Arrays.copyOf(mBuffer, endIndex);
        if (endIndex != mCurIndex) {
            for (int i = 0; i < mCurIndex - endIndex; i++) {
                mBuffer[i] = mBuffer[endIndex + i];
            }
            mCurIndex = mCurIndex - endIndex;
        } else {
            mCurIndex = 0;
        }
        return buf;
    }


    private Tem checkTemAndPos(byte[] ts) {
        Tem tem = new Tem();
        int index = 6;
        float min = 100;
        float max = 0;
        int maxX = 0, maxY = 0, minX = 0, minY = 0, minIndex = 0, maxIndex = 0;

        while (index <= ts.length - 6) {
            float currentTem = DataFormatUtil.bytesToFloat(ts[index], ts[index + 1]) / 10;
            if (currentTem < 100) {
                if (min > currentTem) {
                    min = currentTem;
                    minIndex = index;
                }
                if (max < currentTem) {
                    max = currentTem;
                    maxIndex = index;
                }
            }
            index += 2;
        }

        tem.setMax(max);
        maxX = ((maxIndex - 6 + 1) / 2) % 32;
        maxY = (maxIndex - 6) / 64;
        tem.setMaxX(maxX);
        tem.setMaxY(maxY);

        tem.setMin(min);
        minX = ((minIndex - 6 + 1) / 2) % 32;
        minY = (minIndex - 6) / 64;
        tem.setMinX(minX);
        tem.setMinY(minY);
        return tem;
    }


    class Tem {
        float max;
        float min;
        int maxX, maxY, minX, minY;

        public float getMax() {
            return max;
        }

        public void setMax(float max) {
            this.max = max;
        }

        public float getMin() {
            return min;
        }

        public void setMin(float min) {
            this.min = min;
        }

        public int getMaxX() {
            return maxX;
        }

        public void setMaxX(int maxX) {
            this.maxX = maxX;
        }

        public int getMaxY() {
            return maxY;
        }

        public void setMaxY(int maxY) {
            this.maxY = maxY;
        }

        public int getMinX() {
            return minX;
        }

        public void setMinX(int minX) {
            this.minX = minX;
        }

        public int getMinY() {
            return minY;
        }

        public void setMinY(int minY) {
            this.minY = minY;
        }
    }

    public float getAverageTemperature(byte[] bs) {
        Tem tem = checkTemAndPos(bs);
        return tem.max;
    }
}
