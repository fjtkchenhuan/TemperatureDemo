package com.ys.temperaturelib.device.i2cpoint;

import com.ys.temperaturelib.device.IPointThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;

public class IHM_TSEV01CL55_HLDL extends IPointThermometer {
    public static final String MODE_NAME = "HM-TSEV01CL55-HLDL(单点)";
    static final String ITSEV01CL55_3288 = "/sys/devices/platform/tsev01cl55/value";

    public IHM_TSEV01CL55_HLDL() {
        super(ITSEV01CL55_3288);
        setParm(new MeasureParm(MODE_NAME, 0, 150, 0, 0));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[3];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(3.11f);
        entities[0] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(5.78f);
        entities[1] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(50);
        entity3.setTakeTemperature(8.33f);
        entities[2] = entity3;

        return entities;
    }

    @Override
    public float check(float value,float ta) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        return value + takeTempEntity.getTakeTemperature();
    }
}
