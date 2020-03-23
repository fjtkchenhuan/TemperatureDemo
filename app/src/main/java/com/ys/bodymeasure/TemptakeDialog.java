package com.ys.bodymeasure;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ys.temperaturelib.device.MeasureDevice;
import com.ys.temperaturelib.temperature.TakeTempEntity;

public class TemptakeDialog implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Dialog mDialog;
    private ListView mListView;
    private EditText takeEdit, distanceEdit;
    private Button saveButton;
    private Context mContext;
    MeasureDevice mDevice;
    TakeTempCallback tempCallback;
    TempAdapter tempAdapter;

    public TemptakeDialog(Context context, MeasureDevice device, TakeTempCallback tempCallback) {
        mContext = context;
        mDialog = new Dialog(context);
        mDevice = device;
        this.tempCallback = tempCallback;
    }

    public void show() {
        mDialog.show();
        Window window = mDialog.getWindow();
        window.setContentView(R.layout.dialog_takeview);
        mListView = window.findViewById(R.id.temp_list);
        takeEdit = window.findViewById(R.id.temp_edit_take);
        distanceEdit = window.findViewById(R.id.temp_edit_distance);
        saveButton = window.findViewById(R.id.temp_edit_save);
        saveButton.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        tempAdapter = new TempAdapter();
        mListView.setAdapter(tempAdapter);
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public void distroy() {
        mDialog.dismiss();
        mDialog = null;
    }

    @Override
    public void onClick(View view) {
        Editable takeT = takeEdit.getText();
        Editable distance = distanceEdit.getText();
        if (!TextUtils.isEmpty(takeT)) {
            mDevice.getTakeTempEntity().setTakeTemperature(Float.valueOf(takeT.toString()));
        }
        if (!TextUtils.isEmpty(distance)) {
            mDevice.getTakeTempEntity().setDistances(Integer.valueOf(distance.toString()));
        }
        tempCallback.callback(mDevice.getTakeTempEntity());
        dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TakeTempEntity selectEntity = tempAdapter.getItem(i);
        mDevice.setTakeTempEntity(selectEntity);
        tempCallback.callback(selectEntity);
        dismiss();
    }

    class TempAdapter extends BaseAdapter {
        TakeTempEntity[] entities;

        public TempAdapter() {
            TakeTempEntity[] tempEntities = mDevice.getDefaultTakeTempEntities();
            entities = new TakeTempEntity[tempEntities.length + 1];
            TakeTempEntity entity = new TakeTempEntity();
            entity.setDistances(-1);
            entity.setNeedCheck(false);
            System.arraycopy(tempEntities, 0, entities, 0, tempEntities.length);
            entities[entities.length - 1] = entity;
        }

        @Override
        public int getCount() {
            return entities == null ? 0 : entities.length;
        }

        @Override
        public TakeTempEntity getItem(int i) {
            return entities[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView textView = new TextView(mContext);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(layoutParams);
            layoutParams.gravity = Gravity.RIGHT;
            textView.setGravity(Gravity.RIGHT);
            textView.setPadding(10, 10, 10, 10);
            if(entities[i].getDistances() <0){
                textView.setText("未校准");
            }else{
                textView.setText(entities[i].getDistances() + "cm");
            }
            textView.setTextSize(20);
            return textView;
        }
    }

    public interface TakeTempCallback {
        void callback(TakeTempEntity tempEntity);
    }
}
