package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.device.MeasureResult;
import com.ys.temperaturelib.device.SThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;

import java.util.List;

public abstract class ProductImp extends SThermometer {

    public ProductImp(String device, int baudrate, MeasureParm parm) {
        super();
        setDevice(device);
        setBaudrate(baudrate);
        setParm(parm);
    }

    public void startUp(MeasureResult result, String devicePath, int deviceRate) {
        setDevice(devicePath);
        setBaudrate(deviceRate);
        startUp(result, getParm().perid);
    }

    float lastValidValue = 0f;
    int checkCount = 0;

    protected float getValid(List<Float> data, int maxIndex, int xCount) {
        int nowIndex = maxIndex;
        int preIndex = ((nowIndex / xCount) - 1) * xCount + (nowIndex % xCount);
        int nextIndex = ((nowIndex / xCount) + 1) * xCount + (nowIndex % xCount);
        float[] values = new float[9];
        values[0] = data.get(nowIndex);
        if (values[0] > 42 || values[0] < 30) {
            return values[0];
        }
        if (nowIndex - 1 > (((nowIndex / xCount)) * xCount)) {
            values[1] = data.get(nowIndex - 1);
        }
        if (nowIndex + 1 < ((nowIndex / xCount) + 1) * xCount) {
            values[2] = data.get(nowIndex + 1);
        }
        if (preIndex >= 0) {
            values[3] = data.get(preIndex);
            if (preIndex - 1 > (((preIndex / xCount)) * xCount)) {
                values[4] = data.get(preIndex - 1);
            }
            if (preIndex + 1 < ((preIndex / xCount) + 1) * xCount) {
                values[5] = data.get(preIndex + 1);
            }
        }

        if (nextIndex < data.size()) {
            values[6] = data.get(nextIndex);
            if (nextIndex - 1 > (((nextIndex / xCount)) * xCount)) {
                values[7] = data.get(nextIndex - 1);
            }
            if (nextIndex + 1 < ((nextIndex / xCount) + 1) * xCount) {
                values[8] = data.get(nextIndex + 1);
            }
        }
        float sum = 0;
        int valid = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == 0) continue;
            if (values[0] - values[i] <= 2) {
                sum += values[i];
                valid++;
            }
        }
        float curvalue = sum / valid;
        if (checkCount > 0 && checkCount % 5 != 0) {
            curvalue = lastValidValue;
        }
        checkCount++;
        if (curvalue >= 30 && curvalue <= 42)
            lastValidValue = curvalue;
        if (curvalue < 36 && curvalue >= 30) {
            int value = (int) (curvalue * 100);
            String tempStr = value + "";
            tempStr = tempStr.substring(2, 4);
            curvalue = Float.parseFloat("36." + tempStr);
        }
        return curvalue;
    }

    /**
     * 获取数据输出类型指令
     *
     * @param isAuto 是否自动输出数据
     * @return
     */
    public abstract byte[] getOrderDataOutputType(boolean isAuto);

    /**
     * 获取查询模式下输出指令
     *
     * @return
     */
    public abstract byte[] getOrderDataOutputQuery();

}
