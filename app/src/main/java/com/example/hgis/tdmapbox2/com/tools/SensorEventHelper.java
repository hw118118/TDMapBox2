package com.example.hgis.tdmapbox2.com.tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.mapbox.mapboxsdk.annotations.MarkerView;

/**
 * Created by HGIS on 2017/8/26.
 */

public class SensorEventHelper implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private long lastTime=0;
    private final int TIME_SENSOR=100;
    private float angle;
    private Context mContext;
    private MarkerView mMarker;
    public SensorEventHelper(Context context){
        mContext=context;
        sensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }
    public void registerSensorListener(){
        sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void unRegisterSensorListener(){
        sensorManager.unregisterListener(this,sensor);
    }
    public void setCurrentMarker(MarkerView marker){
        mMarker=marker;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(System.currentTimeMillis()-lastTime<TIME_SENSOR){
            return;
        }
        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ORIENTATION:{
                float x=sensorEvent.values[0];
                x+=getScreenRotationOnPhone(mContext);
                x%=360.0F;
                if(x>180.0F){
                    x-=360.0F;
                }else if (x<-180.0F){
                    x+=360.0F;
                }
                if(Math.abs(angle-x)<3.0f){
                    break;
                }
                angle=Float.isNaN(x) ? 0:x;
                if(mMarker!=null){
                    mMarker.setRotation(angle-360);
                }
                lastTime= System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    /**
     * 获取当前屏幕旋转角度
     *
     * @param context
     * @return 0表示竖屏;90表示是做横屏;180表示是反向竖屏;270表示有横屏
     */
    public static int getScreenRotationOnPhone(Context context){
        final Display display= ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()){
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return -90;
        }
        return 0;
    }
}
