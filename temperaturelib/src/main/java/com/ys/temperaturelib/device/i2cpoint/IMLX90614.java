package com.ys.temperaturelib.device.i2cpoint;

import com.ys.temperaturelib.device.IPointThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.utils.DeviceUtil;
import java.util.ArrayList;
import java.util.List;

public class IMLX90614 extends IPointThermometer {
    public static final String MODE_NAME = "MLX90614(单点)";

    enum IPoint {
        _3288("/sys/bus/i2c/devices/i2c-4/4-005a/mlx90614"),
        _3188("/sys/devices/20072000.i2c/i2c-0/0-005a/mlx90614");
        public String path;

        IPoint(String path) {
            this.path = path;
        }
    }

    public IMLX90614() {
        super(getPath());
        setParm(new MeasureParm(MODE_NAME, 0, 100, 0, 0));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[5];
//        TakeTempEntity entity2 = new TakeTempEntity();
//        entity2.setDistances(3);
//        entity2.setTakeTemperature(1.98f);
//        entities[0] = entity2;
//
//        TakeTempEntity entity1 = new TakeTempEntity();
//        entity1.setDistances(30);
//        entity1.setTakeTemperature(9.78f);
//        entities[1] = entity1;

        TakeTempEntity entity0 = new TakeTempEntity();
        entity0.setDistances(40);
        entity0.setTakeTemperature(1.3f); //-0.3   -0.4  -0.4
        entities[0] = entity0;

        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(50);
        entity1.setTakeTemperature(2.7f);//-0.5  -0.4  -0.4
        entities[1] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(60);
        entity2.setTakeTemperature(3.6f);//-1.1  -0.2  -1.1
        entities[2] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(70);
        entity3.setTakeTemperature(3.9f);//-2  -0.3  -1.5
        entities[3] = entity3;

        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(80);
        entity4.setTakeTemperature(4.4f);//-1.9  -0.3  -2
        entities[4] = entity4;

        return entities;
    }

    int count = 0;
    List<Float> mFloats = new ArrayList<>();
    float lastTemp = 0;
    int tempCount = 0;

    @Override
    public float check(float value, float ta) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        count++;
        mFloats.add(value);
        if (mFloats.size() == 6) {
            tempCount = 5;
        } else if (mFloats.size() > 6) {
            List<Float> floats = mFloats.subList(tempCount - 3, tempCount - 3 + 5);
            float sum = 0;
            float max = floats.get(0);
            float min = floats.get(0);

            for (int i = 0; i < floats.size(); i++) {
                sum += floats.get(i);
                if (floats.get(i) > max) max = floats.get(i);
                if (floats.get(i) < min) min = floats.get(i);
            }
            float tt = (sum / 5f);
            tt += takeTempEntity.getTakeTemperature();
            if (tt >= 34f && tt <= 36f) {
                int tt1 = (int) (tt * 100);
                tt1 += getParm().isLight ? -0.9f : 0f;
                tt = Float.parseFloat("36." + String.valueOf(tt1).substring(2, 4));
            } else if (tt >= 37.2f && tt <= 37.5f) {
                tt += getParm().isLight ? -0.9f : 0f;
                tt += 0.3f;
            }
            getStorager().add(tempCount + ":" + floats + " t:" + tt);
            lastTemp = tt;
            tempCount++;
            return tt;
        }
        return lastTemp;
    }

    private static String getPath() {
        String product = DeviceUtil.getRKModel();
        IPoint point = IPoint._3288;
        if (product.contains("rk3128")) {
            point = IPoint._3188;
        }
        return point.path;
    }
}
