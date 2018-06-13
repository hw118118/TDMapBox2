package com.example.hgis.tdmapbox2.com.route_plan;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.widget.ImageView;

import com.mapbox.mapboxsdk.maps.MapView;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by HGIS on 2017/9/19.
 */

public class RoutePlanActivity  extends Activity {
    private static final String TAG=RoutePlanActivity.class.getName();
    private MaterialEditText mEditStart;
    private MaterialEditText mEditEnd;
    private ImageView mImageBack;
    private ImageView mImageReturn;
    private TabLayout mTabLayout;
    private MapView mMapView;
}
