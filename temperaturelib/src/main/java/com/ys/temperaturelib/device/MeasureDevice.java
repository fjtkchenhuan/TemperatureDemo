package com.ys.temperaturelib.device;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;

/**
 * 测量设备类，主要有初始化、启动、下发指令、销毁等方法。
 * 用于设备数据读取、指令下发。
 */
public abstract class MeasureDevice {

    /**
     * 初始化设备
     *
     * @return 成功返回true 失败返回false
     */
    protected abstract boolean init();


    /**
     * 启动设别
     *
     * @param result 结果回调
     * @param period 间隔 millis
     */
    public abstract void startUp(MeasureResult result, long period);


    /**
     * 下发设备指令
     *
     * @param data
     */
    public abstract void order(byte[] data);

    /**
     * 销毁设备
     */
    public abstract void destroy();

    /**
     * 校验温度值
     *
     * @param value
     * @param ta
     * @return
     */
    public abstract float check(float value, float ta);

    /**
     * 是否为单点输出
     *
     * @return
     */
    public abstract boolean isPoint();


    TakeTempEntity mTakeTempEntity;

    public TakeTempEntity[] getDefaultTakeTempEntities() {
        return null;
    }

    public TakeTempEntity getTakeTempEntity() {
        return mTakeTempEntity;
    }

    public void setTakeTempEntity(TakeTempEntity takeTempEntity) {
        mTakeTempEntity = takeTempEntity;
    }

    MeasureParm mParm;

    public MeasureParm getParm() {
        return mParm;
    }

    public void setParm(MeasureParm parm) {
        mParm = parm;
    }

}
