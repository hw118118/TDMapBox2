package com.example.hgis.tdmapbox2.com;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hgis.tdmapbox2.MapStyleDialog;
import com.example.hgis.tdmapbox2.R;
import com.example.hgis.tdmapbox2.com.tools.SensorEventHelper;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyBearingTracking;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.TrackingSettings;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.maps.widgets.MyLocationViewSettings;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.tianditu.android.maps.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.hgis.tdmapbox2.R.mipmap.navi_map_gps_locked;
/**
 * Created by hw on 2017/10/11.
 */

public class DetailrouteActivty extends AppCompatActivity implements PermissionsListener, View.OnClickListener{
    private MapView mapView;
    private MapboxMap map;
    private FloatingActionButton floatingActionButton;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private PermissionsManager permissionsManager;
    private ImageView switch_mapstyle;
    private ImageView navigation_zoom_in;
    private ImageView navigation_zoom_out;
    Double maxzoom = 0.0;
    Double minzoom = 0.0;
    TrackingSettings trackingSettings;
    private Location nowLocation;
    /**
     * 地图风格切换
     */
    MyLocationViewSettings locationSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(savedInstanceState);
        addListener();
    }

    /**
     * 初始化
     * @param savedInstanceState
     */
    private void initView(Bundle savedInstanceState) {
        Mapbox.getInstance(this, getString(R.string.accessToken));
        setContentView(R.layout.detail_route);
        //获取定位引擎并激活
        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();
        mapView = (MapView) findViewById(R.id.mapView1);
        mapView.onCreate(savedInstanceState);  //重写此方法
        Intent lastintent = getIntent();
        String listPoints = lastintent.getStringExtra("points");
        final ArrayList<LatLng> points = new ArrayList<>();
        String[] list = listPoints.split(";");
        for (int i=0;i<list.length;i++){
            LatLng latLng = new LatLng(Double.valueOf(list[i].split(",")[0]).doubleValue(), Double.valueOf(list[i].split(",")[1]).doubleValue());
            points.add(latLng);
        }
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                map.setStyleUrl("asset://satellite.json");
                CameraPosition position = new CameraPosition.Builder()
                        .target(points.get(points.size()/2))
                        .zoom(11)
//                        .bearing(90)
//                        .tilt(90)
                        .build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                UiSettings uiSettings = map.getUiSettings();
                uiSettings.setLogoEnabled(false);
                uiSettings.setAttributionEnabled(false);
                //设置指南针
                uiSettings.setCompassGravity(0);
                uiSettings.setCompassMargins(50, 220, 0, 0);
                maxzoom = map.getMaxZoomLevel();
                minzoom = map.getMinZoomLevel();
                locationSettings = map.getMyLocationViewSettings();
                locationSettings.setBackgroundDrawable(ContextCompat.getDrawable(DetailrouteActivty.this, R.mipmap.navi_map_gps_locked), new int[]{0, 0, 0, 0});
                locationSettings.setForegroundTintColor(ContextCompat.getColor(DetailrouteActivty.this, R.color.nav));
                locationSettings.setAccuracyTintColor(ContextCompat.getColor(DetailrouteActivty.this                                                                                                                                                                                                                                                                                                                                                                                                   ,R.color.nav_border));
//                locationSettings.setAccuracyAlpha(50);
//                locationSettings.setTilt(30);
                trackingSettings= map.getTrackingSettings();
                // 让地图始终以定位点为中心，无法滑动
                trackingSettings.setDismissAllTrackingOnGesture(true);
                // 启用位置和方位跟踪
                trackingSettings.setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                trackingSettings.setMyBearingTrackingMode(MyBearingTracking.COMPASS);
                mapboxMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .color(Color.parseColor("#3bb2d0"))
                        .width(2));
            }
        });
        //地图的放大缩小
        navigation_zoom_in = (ImageView) findViewById(R.id.navigation_zoom_in1);
        navigation_zoom_out = (ImageView) findViewById(R.id.navigation_zoom_out1);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab1);
        //地图风格切换
        /**
         *  通过startActivityForResult
         */
        switch_mapstyle = (ImageView) findViewById(R.id.iv_map_type_switch1);

    }

    private void addListener() {
        navigation_zoom_out.setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);
        switch_mapstyle.setOnClickListener(this);
        navigation_zoom_in.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.navigation_zoom_in1:
                zoom_in();
                break;
            case R.id.navigation_zoom_out1:
                zoom_out();
                break;
            case R.id.location_toggle_fab1:
                if (map != null) {
                    toggleGps();
                }
                break;
            case R.id.iv_map_type_switch1:
                showMapStyleDialog();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "未获得位置许可",
                Toast.LENGTH_LONG).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            enableLocation();
        }else{
            Toast.makeText(this,"您没有获得位置许可",Toast.LENGTH_LONG).show();
            finish();
        }
    }
    public void toggleGps() {
        permissionsManager = new PermissionsManager(this);
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager.requestLocationPermissions(this);
        } else {
            enableLocation();
        }
    }

    public Location getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return nowLocation;
        }
        //获取上次的定位参数  若存在直接使用
        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation!=null){
            nowLocation = lastLocation;
        }
        locationEngineListener=new LocationEngineListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onLocationChanged(Location location) {
                if(location!=null){
                    nowLocation = location;
                }
            }
        };
        return nowLocation;
    }

    public void enableLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        //获取上次的定位参数  若存在直接使用
        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation!=null){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 17),2000);
        }
        locationEngineListener=new LocationEngineListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onLocationChanged(Location location) {
                if(location!=null){
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 17),2000);
                    locationEngine.removeLocationEngineListener(this);
                }
            }
        };
//        locationEngine.addLocationEngineListener(locationEngineListener);
        //添加移除定位图层
        map.setMyLocationEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode==2){
            String result=data.getStringExtra("map_type");
            switch (result){
                case "normal":
                    map.setStyle(Style.LIGHT);
                    break;
                case "satellite":
                    map.setStyle(Style.SATELLITE_STREETS);
                    break;
            }
        }

    }

    private void showMapStyleDialog(){
        //加载mapstyle界面布局文件
//        RelativeLayout mapstyle= (RelativeLayout) getLayoutInflater().inflate(R.layout.switch_mapstyle,null);
//        AlertDialog.Builder builder=new AlertDialog.Builder(this);
//        builder.setView(mapstyle);
//        AlertDialog dialog=builder.create();
//        dialog.show();
        final MapStyleDialog mapStyleDialog=new MapStyleDialog(this,R.style.dialog_no_border,new MapStyleDialog.LeaveMapStyleDialogListener(){
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.rl_vector:
                        map.setStyle(Style.MAPBOX_STREETS);
                        break;
                    case R.id.rl_satellite:
                        map.setStyleUrl("asset://satellite.json");
                        break;
                    default:
                        break;
                }
            }
        });
        Window mWindow = mapStyleDialog.getWindow();
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        /**
         * lp.x与lp.y表示相对于原始位置的偏移.
         * 当参数值包含Gravity.LEFT时,对话框出现在左边,所以lp.x就表示相对左边的偏移,负值忽略.
         * 当参数值包含Gravity.RIGHT时,对话框出现在右边,所以lp.x就表示相对右边的偏移,负值忽略.
         * 当参数值包含Gravity.TOP时,对话框出现在上边,所以lp.y就表示相对上边的偏移,负值忽略.
         * 当参数值包含Gravity.BOTTOM时,对话框出现在下边,所以lp.y就表示相对下边的偏移,负值忽略.
         * 当参数值包含Gravity.CENTER_HORIZONTAL时
         * ,对话框水平居中,所以lp.x就表示在水平居中的位置移动lp.x像素,正值向右移动,负值向左移动.
         * 当参数值包含Gravity.CENTER_VERTICAL时
         * ,对话框垂直居中,所以lp.y就表示在垂直居中的位置移动lp.y像素,正值向右移动,负值向左移动.
         * gravity的默认值为Gravity.CENTER,即Gravity.CENTER_HORIZONTAL |
         * Gravity.CENTER_VERTICAL.
         *
         * 本来setGravity的参数值为Gravity.LEFT | Gravity.TOP时对话框应出现在程序的左上角,但在
         * 我手机上测试时发现距左边与上边都有一小段距离,而且垂直坐标把程序标题栏也计算在内了,
         * Gravity.LEFT, Gravity.TOP, Gravity.BOTTOM与Gravity.RIGHT都是如此,据边界有一小段距离
         */

//

//        lp.width = 300; // 宽度
//        lp.height = 300; // 高度
//        lp.alpha = 0.7f; // 透明度

        mWindow.setGravity(Gravity.RIGHT|Gravity.TOP);  //设置dilog显示的位置
        lp.x =45; // 新位置Y坐标
        lp.y = 340; // 新位置X坐标
        mWindow.setAttributes(lp);
        mWindow.setWindowAnimations(R.style.dialogWindowAnim);  //添加动画
        mapStyleDialog.show();
    }
    //地图缩放
    private void zoom_out(){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                Double zoom=mapboxMap.getCameraPosition().zoom;
                if((zoom-minzoom)<0.000005) {
                    navigation_zoom_out.setBackgroundResource(R.mipmap.zoomout_disable);
                }else {
                    navigation_zoom_in.setBackgroundResource(R.mipmap.zoomin);
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapboxMap.getCameraPosition().target), zoom - 1), 1);
                }
            }
        });
    }
    private void zoom_in(){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                Double zoom=mapboxMap.getCameraPosition().zoom;
                if((maxzoom-zoom)<0.000005){
                    navigation_zoom_in.setBackgroundResource(R.mipmap.zoomin_disable);
                }else{
                    navigation_zoom_out.setBackgroundResource(R.mipmap.zoomout);
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapboxMap.getCameraPosition().target),zoom+1),145);
                }
            }
        });
    }
}
