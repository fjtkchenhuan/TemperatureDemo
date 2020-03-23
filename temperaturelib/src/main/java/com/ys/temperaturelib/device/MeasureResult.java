package com.ys.temperaturelib.device;

import com.ys.temperaturelib.temperature.TemperatureEntity;

public interface MeasureResult<T> {
    void onResult(TemperatureEntity entity,T oneFrame);
}
