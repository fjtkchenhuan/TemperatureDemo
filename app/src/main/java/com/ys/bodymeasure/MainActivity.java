package com.ys.bodymeasure;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ys.temperaturelib.depth.DepthImpl;

public class MainActivity extends PermissionActivity {
    AlertDialog mI2CDialog;
    AlertDialog mSerialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        i2cDialog();
        serialDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mI2CDialog != null) mI2CDialog.dismiss();
        mI2CDialog = null;
        if (mSerialDialog != null) mSerialDialog.dismiss();
        mSerialDialog = null;
    }

    private void i2cDialog() {
        if (mI2CDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(I2CActivity.MODE_I2C, 0,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, I2CActivity.class);
                            intent.putExtra("device_index", which);
                            startActivity(intent);
                            mI2CDialog.dismiss();
                        }
                    });
            mI2CDialog = builder.create();
        }
    }

    private void serialDialog() {
        if (mSerialDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(SerialActivity.MODE_SERIALPORT, 0,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, SerialActivity.class);
                            intent.putExtra("device_index", which);
                            startActivity(intent);
                            mSerialDialog.dismiss();
                        }
                    });
            mSerialDialog = builder.create();
        }
    }

    public void onI2CMeasure(View view) {
        if (mI2CDialog != null) mI2CDialog.show();
    }

    public void onSerialMeasure(View view) {
        if (mSerialDialog != null) mSerialDialog.show();
    }
}
