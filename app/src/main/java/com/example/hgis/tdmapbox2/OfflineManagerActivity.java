package com.example.hgis.tdmapbox2;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by HGIS on 2017/8/16.
 */

public class OfflineManagerActivity extends Activity implements View.OnClickListener{

    private static final String TAG="OfflineManagerActivity";
    //JSON encoding/decoding
    public static final String JSON_CHARSET="UTF-8";
    public static final String JSON_FIELD_REGION_NAME="FIELD_REGION_NAME";

    //UI elements
    private ImageView iv_back;
    private Button download_button;
    private Button list_button;
    private ProgressBar progressBar;
    private MapboxMap map;
    private MapView mapView;
    private boolean isEndNotified;
    private int regionSelected;
    //Offline objects
    private OfflineManager offlineManager;
    private OfflineRegion offlineRegion;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);
        addListener();
    }
    private void init(Bundle savedInstanceState){
        Mapbox.getInstance(this,getString(R.string.accessToken));
        setContentView(R.layout.offlinemap);
        ButterKnife.bind(this);
        mapView =findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map=mapboxMap;
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(30.6, 114.30))
                        .zoom(10)
                        .build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
            }
        });
        //建立offlineManager
        offlineManager=OfflineManager.getInstance(this);
        iv_back=findViewById(R.id.iv_back);
        download_button=findViewById(R.id.download_button);
        list_button=findViewById(R.id.list_button);
        progressBar = findViewById(R.id.progress_bar);
    }
    private void addListener(){
        iv_back.setOnClickListener(this);
        download_button.setOnClickListener(this);
        list_button.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.download_button:
                downloadRegionDialog();
                break;
            case R.id.list_button:
                downloadedRegionList();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    private void downloadRegionDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final EditText regionNameEdit=new EditText(this);
        regionNameEdit.setHint(getString(R.string.set_region_name_hint));
        //设置dialog属性
        builder.setTitle(getString(R.string.dialog_title))
        .setView(regionNameEdit)
        .setMessage(getString(R.string.dialog_message))
        .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String regionName=regionNameEdit.getText().toString();
                if (regionName.length()==0){
                    Toast.makeText(OfflineManagerActivity.this, getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
                }else{
                    downloadRegion(regionName);
                }
            }
        })
        .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
    private void downloadRegion(final String regionName){
        //定义离线区域参数 包括边界  缩放级别 元数据
        startProgress();
        String styleUrl=map.getStyleUrl();
        LatLngBounds bounds=map.getProjection().getVisibleRegion().latLngBounds;
        double minZoom=map.getCameraPosition().zoom;
        double maxZoom=map.getMaxZoomLevel();
        float pixelRatio=this.getResources().getDisplayMetrics().density;
        OfflineTilePyramidRegionDefinition definition=new OfflineTilePyramidRegionDefinition(styleUrl,bounds,minZoom,maxZoom,pixelRatio);

        //创建元数据变量
        byte[] metadata;
        try{
            JSONObject jsonObject=new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME,regionName);
            String json=jsonObject.toString();
            metadata=json.getBytes(JSON_CHARSET);
        }catch (Exception e){
            Log.e(TAG, "Failed to encode metadata: " + e.getMessage());
            metadata = null;
        }
        //创建离线区域 开启下载
        offlineManager.createOfflineRegion(definition,metadata,new OfflineManager.CreateOfflineRegionCallback(){

            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                Log.d(TAG, "Offline region created: " + regionName);
                OfflineManagerActivity.this.offlineRegion=offlineRegion;
                launchDownload();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void launchDownload() {
        //建立观察者模式去管理下载进度
        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                double percentage=status.getRequiredResourceCount()>=0?(100.0*status.getCompletedResourceCount()/status.getRequiredResourceCount()):0.0;
                Log.i("tag",status.getCompletedTileCount()+"");
                if (status.isComplete()){
                    endProgress(getString(R.string.end_progress_success));
                    return;
                }else if (status.isRequiredResourceCountPrecise()){
                    setPercentage((int) Math.round(percentage));
                }
                Log.d(TAG, String.format("%s/%s resources; %s bytes downloaded.",
                        String.valueOf(status.getCompletedResourceCount()),
                        String.valueOf(status.getRequiredResourceCount()),
                        String.valueOf(status.getCompletedResourceSize())));
            }

            @Override
            public void onError(OfflineRegionError error) {
                Log.e(TAG, "onError reason: " + error.getReason());
                Log.e(TAG, "onError message: " + error.getMessage());
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
            }
        });
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }
    private void downloadedRegionList(){
        regionSelected=0;
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                if (offlineRegions==null||offlineRegions.length==0){
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<String> offlineRegionsNames=new ArrayList<String>();
                for(OfflineRegion offlineRegion:offlineRegions){
                    offlineRegionsNames.add(getRegionName(offlineRegion));
                }
                final CharSequence[] items=offlineRegionsNames.toArray(new CharSequence[offlineRegionsNames.size()]);
                AlertDialog dialog=new AlertDialog.Builder(OfflineManagerActivity.this)
                        .setTitle(getString(R.string.navigate_title))
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                regionSelected=i;
                            }
                        })
                        .setPositiveButton(getString(R.string.navigate_positive_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(OfflineManagerActivity.this,items[regionSelected],Toast.LENGTH_LONG).show();
                                LatLngBounds bounds=((OfflineTilePyramidRegionDefinition) offlineRegions[regionSelected].getDefinition()).getBounds();
                                double regionZoom=((OfflineTilePyramidRegionDefinition) offlineRegions[regionSelected].getDefinition()).getMinZoom();
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(bounds.getCenter())
                                        .zoom(regionZoom)
                                        .build();
                                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            }
                        })
                        .setNeutralButton(getString(R.string.navigate_neutral_button_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                progressBar.setIndeterminate(true);
                                progressBar.setVisibility(View.VISIBLE);
                                offlineRegions[regionSelected].delete(new OfflineRegion.OfflineRegionDeleteCallback(){
                                    @Override
                                    public void onDelete() {
                                        progressBar.setVisibility(View.VISIBLE);
                                        progressBar.setIndeterminate(false);
                                        Toast.makeText(getApplicationContext(),getString(R.string.toast_region_deleted),Toast.LENGTH_LONG).show();

                                    }

                                    @Override
                                    public void onError(String error) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        progressBar.setIndeterminate(false);
                                        Log.e(TAG, "Error: " + error);
                                    }
                                });
                            }
                        })
                        .setNegativeButton(getString(R.string.navigate_negative_button_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                        dialog.show();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private String getRegionName(OfflineRegion offlineRegion) {
        //Get region name from the offline region metadata
        String regionName;
        try{
            byte[] metadata=offlineRegion.getMetadata();
            String json=new String(metadata,JSON_CHARSET);
            JSONObject jsonObject=new JSONObject(json);
            regionName=jsonObject.getString(JSON_FIELD_REGION_NAME);
        }catch (Exception e){
            Log.e(TAG,"Falied to decode metadata:"+e.getMessage());
            regionName = String.format(getString(R.string.region_name), offlineRegion.getID());
        }
        return  regionName;
    }

    private void startProgress(){
        download_button.setEnabled(false);
        list_button.setEnabled(false);
        //展示进度条
        isEndNotified=false;
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }
    private void setPercentage(final int percentage){
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }
    private void endProgress(final String message){
        if (isEndNotified){
            return;
        }
        download_button.setEnabled(true);
        list_button.setEnabled(true);

        isEndNotified=true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        Toast.makeText(OfflineManagerActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
