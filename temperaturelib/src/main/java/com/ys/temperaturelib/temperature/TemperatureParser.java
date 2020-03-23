package com.ys.temperaturelib.temperature;

/**
 * 解析温度数据
 *
 * @param <T>
 */
public interface TemperatureParser<T> {
    /**
     * 处理一帧数据 当有分包数据时，合包后返回一帧数据，分包数据返回null
     *
     * @param data 一帧数据
     * @return
     */
    public T oneFrame(T data);

    /**
     * 解析一帧数据为温度数据
     *
     * @param data 一帧数据
     * @return
     */
    public TemperatureEntity parse(T data);
}
