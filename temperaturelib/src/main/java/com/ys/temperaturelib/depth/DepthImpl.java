package com.ys.temperaturelib.depth;

import android.content.Context;
import android.util.Log;

import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiDeviceAttribute;
import com.hjimi.api.iminect.ImiFrameMode;
import com.hjimi.api.iminect.ImiNect;

import java.nio.ByteBuffer;

public class DepthImpl {
    static final String TAG = "DepthImpl";
    ImiDevice mDevice;
    Context context;
    private boolean mShouldRun = false;
    private ImiDevice.ImiStreamType mStreamType = ImiDevice.ImiStreamType.DEPTH;
    private int m_DepthValue = -1;
    private int mFps = -1;
    private int mCount = -1;
    private int m_width = -1;
    private int m_height = -1;
    DepthRunner mDepthRunner;
    DepthResult mDepthResult;

    public DepthImpl(Context context, DepthResult depthResult) {
        this.context = context;
        mDepthResult = depthResult;
        ImiNect.initialize();
        mDevice = ImiDevice.getInstance();
        mDevice.open(context, 0, new ImiDevice.OpenDeviceListener() {

            @Override
            public void onOpenDeviceSuccess() {
                ImiDeviceAttribute mDeviceAttribute = mDevice.getAttribute();
                Log.i(TAG, "Device SerialNumber : " + mDeviceAttribute.getSerialNumber());

                ImiFrameMode frameMode = mDevice.getCurrentFrameMode(ImiDevice.ImiStreamType.DEPTH);
                mDevice.setFrameMode(ImiDevice.ImiStreamType.DEPTH, frameMode);
                Log.i(TAG, "Depth frame mode: " + frameMode.getResolutionX() + ", " + frameMode.getResolutionY());
                onStart();
                mDevice.startStream(ImiDevice.ImiStreamType.DEPTH.toNative());
            }

            @Override
            public void onOpenDeviceFailed(String s) {
                Log.e(TAG, "onOpenDeviceFailed : " + s);
            }

        });
    }

    public int getDepthValue() {
        return m_DepthValue;
    }

    public int getFps() {
        return mFps;
    }

    public int getStreamWidth() {
        return m_width;
    }

    public int getStreamHeight() {
        return m_height;
    }


    private void onStart() {
        if (!mShouldRun) {
            mShouldRun = true;
            if (mDepthRunner != null) mDepthRunner.interrupt();
            mDepthRunner = null;
            mDepthRunner = new DepthRunner();
            mDepthRunner.start();
        }
    }

    public void release() {
        mShouldRun = false;
        if (mDepthRunner != null) mDepthRunner.interrupt();
        mDepthRunner = null;
        if (mDevice != null) {
            mDevice.close();
            mDevice.destroy();
            mDevice = null;
        }
        ImiNect.destroy();
    }

    public interface DepthResult {
        void onResult(int distance);
    }

    private class DepthRunner extends Thread {
        @Override
        public void run() {
            long startTime = 0;
            long endTime = 0;
            while (mShouldRun) {
                ImiDevice.ImiFrame nextFrame = mDevice.readNextFrame(mStreamType, 30);
                if (nextFrame == null) {
                    continue;
                }
                mCount++;
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }
                endTime = System.currentTimeMillis();
                if (endTime - startTime >= 1000) {
                    mFps = mCount;
                    mCount = 0;
                    startTime = endTime;
                }
                ByteBuffer frameData = nextFrame.getData();
                int width = nextFrame.getWidth();
                int height = nextFrame.getHeight();
                m_width = width;
                m_height = height;

                int X = width / 2;
                int Y = height / 2;
                int curIndex = width * Y + X;

                frameData.position(curIndex * 2);
                m_DepthValue = (int) ((frameData.get() & 0xFF) | ((frameData.get() & 0xFF) << 8));
                if(mDepthResult != null) mDepthResult.onResult(m_DepthValue);
            }
        }
    }
}
