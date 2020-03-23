package com.ys.bodymeasure;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


/**
 * Created by Administrator on 2018/4/18.
 */

public class PermissionActivity extends AppCompatActivity {
    static final String[] PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasUnCkeck = false;
            for (int i = 0; i < PERMISSION_LIST.length; i++) {
                if (checkSelfPermission(PERMISSION_LIST[i]) != PackageManager.PERMISSION_GRANTED) {
                    hasUnCkeck = true;
                }
            }
            if (hasUnCkeck) {
                requestPermissions(PERMISSION_LIST, 300);
            }
        }
    }
}
