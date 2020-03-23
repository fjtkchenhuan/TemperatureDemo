package com.ys.temperaturelib.utils;

public class DeviceUtil {

    public static String getRKModel() {
        String intern = FileUtil.getValueFromProp("ro.board.platform");
//        String intern1 = Build.PRODUCT.intern();
//        String intern = Build.MODEL.intern();
        if (intern.contains("312x"))
            intern = "rk3128";

        return intern;
    }
}
