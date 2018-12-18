package com.android.gps.tools;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class MyHandler extends Handler {

    private final WeakReference<TextView> gpsTextReference;
    private final WeakReference<TextView> locationTextReference;

    public MyHandler(TextView gpsStatusTxt,TextView locationTxt){
        this.gpsTextReference = new WeakReference<>(gpsStatusTxt);
        this.locationTextReference = new WeakReference<>(locationTxt);
    }

    @Override
    public void handleMessage(Message msg){
        super.handleMessage(msg);

        switch (msg.what){
            case 201:{
                Map<String,Double> map = new HashMap<>();
                map = (Map)msg.obj;

                locationTextReference.get().setText("位置：经度："+map.get("lng")+",纬度："+map.get("lat"));
                break;
            }
            case 202:{
                gpsTextReference.get().setText("搜索到的卫星颗数："+msg.arg1);
                break;
            }
        }
    }
}
