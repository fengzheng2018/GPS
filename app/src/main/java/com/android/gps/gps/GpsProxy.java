package com.android.gps.gps;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;

import com.android.gps.tools.MyHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GpsProxy {

    private Context mContext;
    private static volatile GpsProxy gpsProxy;
    private LocationManager mLocationManager;
    private MyHandler myHandler;

    public GpsProxy(Context mContext,LocationManager locationManager,MyHandler myHandler) {
        this.mContext = mContext;
        this.mLocationManager = locationManager;
        this.myHandler = myHandler;
    }

    /**
     * 获取GpsProxy实例
     */
    public static GpsProxy getInstance(Context context,LocationManager locationManager,MyHandler myHandler){
        if(gpsProxy == null){
            //同步锁
            synchronized (GpsProxy.class){
                gpsProxy = new GpsProxy(context,locationManager,myHandler);
            }
        }
        return gpsProxy;
    }

    /**
     * 初始化定位环境
     */
    public void initLocationEnvironment(){
        //获取最佳位置提供器
        String bestProvider = mLocationManager.getBestProvider(getCriteria(),true);
        //获取最后一个已知位置
        Location location = mLocationManager.getLastKnownLocation(bestProvider);

        //注册监听器
        registerListener();
    }

    /**
     * 注册监听器
     */
    private void registerListener(){
        mLocationManager.addGpsStatusListener(gpsListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,mLocationListener);
    }

    /**
     * 移除监听器
     */
    public void removeListener(){
        mLocationManager.removeGpsStatusListener(gpsListener);
        mLocationManager.removeUpdates(mLocationListener);
    }


    /**
     * 位置监听器
     */
    private LocationListener mLocationListener = new LocationListener() {
        /**
         * 位置变化时调用
         */
        @Override
        public void onLocationChanged(Location location) {
            Message msg = myHandler.obtainMessage();

            Map<String,Double> map = new HashMap<>();
            map.put("lat",location.getLatitude());
            map.put("lng",location.getLongitude());

            msg.what = 201;
            msg.obj = map;

            myHandler.sendMessage(msg);
        }

        /**
         * GPS状态改变时调用
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        /**
         * GPS开启时调用
         */
        @Override
        public void onProviderEnabled(String provider) {

        }

        /**
         * GPS禁用时调用
         */
        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * GPS状态监听器
     */
    private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event){
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:{
                    break;
                }
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:{
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    //获取卫星颗数默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> satelliteIterator = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (satelliteIterator.hasNext() && count <= maxSatellites){
                        GpsSatellite satellite = satelliteIterator.next();
                        //已定位卫星颗数
                        if(satellite.usedInFix()){
                            count ++;
                        }
                    }

                    Message msg = myHandler.obtainMessage();

                    msg.what = 202;
                    msg.arg1 = count;

                    myHandler.sendMessage(msg);
                    break;
                }
                //定位启动
                case GpsStatus.GPS_EVENT_STARTED:{
                    break;
                }
                //定位结束
                case GpsStatus.GPS_EVENT_STOPPED:{
                    break;
                }
            }
        }
    };

    /**
     * 获取最佳位置提供器
     */
    private Criteria getCriteria(){
        Criteria criteria = new Criteria();

        //要求高精度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //是否要求速度
        criteria.setSpeedRequired(false);
        //是否允许运营商收费
        criteria.setCostAllowed(true);
        //是否需要方位信息
        criteria.setBearingRequired(false);
        //是否需要海拔信息
        criteria.setAltitudeRequired(false);
        //对电源需求
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        return criteria;
    }
}
