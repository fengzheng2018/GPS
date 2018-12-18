package com.android.gps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.gps.gps.GpsProxy;
import com.android.gps.tools.MyHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private final int LOCATION_PERMISSION_CODE = 101;
    private final int LOCATION_REQUEST_CODE = 102;
    private TextView gpsStatusTxt;
    private TextView locationTxt;

    private GpsProxy gpsProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;
        gpsStatusTxt = findViewById(R.id.gps_status_txt);
        locationTxt = findViewById(R.id.location_txt);
    }

    @Override
    protected void onStart(){
        super.onStart();

        MyHandler myHandler = new MyHandler(gpsStatusTxt,locationTxt);

        /**
         * 检查定位权限
         */
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            List<String> locationPermission = new ArrayList<>();
            locationPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
            locationPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            ActivityCompat.requestPermissions(MainActivity.this, locationPermission.toArray(new String[locationPermission.size()]), LOCATION_PERMISSION_CODE);
            return;
        }

        LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        //GPS不可用
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,LOCATION_REQUEST_CODE);
            return;
        }

        gpsProxy = GpsProxy.getInstance(mContext,mLocationManager,myHandler);
        gpsProxy.initLocationEnvironment();
    }


    /**
     * 当允许定位权限时
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            //
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        gpsProxy.removeListener();
    }
}
