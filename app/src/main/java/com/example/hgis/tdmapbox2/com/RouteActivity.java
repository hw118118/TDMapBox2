package com.example.hgis.tdmapbox2.com;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.example.hgis.tdmapbox2.MainActivity;
import com.example.hgis.tdmapbox2.R;

import com.tianditu.android.core.LoadServicesURL;
import com.tianditu.android.maps.GeoPoint;

import com.tianditu.android.maps.MapView;
import com.tianditu.android.maps.TBusLineSearch;
import com.tianditu.android.maps.TBusRoute;
import com.tianditu.android.maps.TTransitLine;
import com.tianditu.android.maps.TTransitResult;
import com.tianditu.android.maps.TTransitSegmentInfo;
import com.tianditu.android.maps.TTransitSegmentLine;
import com.tianditu.engine.PoiSearch.SearchParam;
import com.tianditu.android.maps.TBusRoute.OnTransitResultListener;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RouteActivity extends AppCompatActivity implements OnTransitResultListener{
    private ImageView iv_back;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private double startLat;
    private double startLon;
    private EditText startText;
    private EditText endText;
    private GeoPoint startPoint = null;
    private GeoPoint endPoint = null;
    private Button searchButton;
    private List<PageFragment> mFragmentList;
    public static TBusRoute busRoute = null;
    private myFragmentPagerAdapter pagerAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_route);
        mContext = this;
        //返回上一级
        iv_back = (ImageView)findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //结束当前页面
                finish();
            }
        });
        Intent lastintent = getIntent();
        Bundle bundle = lastintent.getExtras();
        startLat  = bundle.getDouble("lat");
        startLon  = bundle.getDouble("lon");
        startText = (EditText)findViewById(R.id.startText);
        endText = (EditText)findViewById(R.id.endText);

        //添加fragment
        initFragment();
        searchButton = (Button)findViewById(R.id.searchButton) ;
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)RouteActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS);


                String start = startText.getText().toString();
                if(start.equals("我的位置")){
                    int latitudeE6 = (int)(startLat * 1000000.0D);
                    int longitudeE6 = (int)(startLon * 1000000.0D);
                    startPoint = new GeoPoint(latitudeE6,longitudeE6);
                }else{
                    try {
                        int latitudeE6 = Integer.parseInt(start.split(",")[0]);
                        int longitudeE6 = Integer.parseInt(start.split(",")[1]);
                        startPoint = new GeoPoint(latitudeE6,longitudeE6);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                String end = endText.getText().toString();
                try {
                    int latitudeE6_end = Integer.parseInt(end.split(",")[0]);
                    int longitudeE6_end = Integer.parseInt(end.split(",")[1]);
                    endPoint = new GeoPoint(latitudeE6_end,longitudeE6_end);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                LoadServicesURL.initLoadServices(RouteActivity.this,"json");
                busRoute = new TBusRoute(RouteActivity.this);
                busRoute.startRoute(startPoint, endPoint, TBusRoute.BUS_TYPE_FASTEST);
            }
        });




        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        pagerAdapter =
                new myFragmentPagerAdapter(getSupportFragmentManager(),this,mFragmentList);
        viewPager.setAdapter(pagerAdapter);//1.设置好ViewPager
        tabLayout.setupWithViewPager(viewPager);//2.设置TabLayout与ViewPager的对应关系
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(pagerAdapter.getTabView(i));//(最关键)3.使用TabLayout的setCustomView设置自定义的TAB_View
            }
        }
        viewPager.setCurrentItem(1);

    }



    @Override
    public void onTransitResult(TTransitResult result, int error) {
        if(result == null||result.getTransitLines().size()==0){
            Toast.makeText(this, "未找到结果", Toast.LENGTH_LONG).show();
            return;


        }else {
            ArrayList<String> data = new ArrayList<String>();
            List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
//            List<Map<String, ArrayList<ArrayList<GeoPoint>>>> points = new ArrayList<Map<String, ArrayList<ArrayList<GeoPoint>>>>();
//            ArrayList<GeoPoint> points0 = new ArrayList<GeoPoint>();
            ArrayList<String> listPoints = new ArrayList<String>();
            data.add("是否有地铁:" + result.hasSubWay());
            for (int i = 0; i < result.getTransitLines().size(); i++) {
//                ArrayList<ArrayList<GeoPoint>> listPoints = new  ArrayList<ArrayList<GeoPoint>>();
                Map<String, String> map = new HashMap<String, String>();
                String points = "";
//                Map<String, ArrayList<ArrayList<GeoPoint>>> map_points = new HashMap<String, ArrayList<ArrayList<GeoPoint>>>();
                TTransitLine line = result.getTransitLines().get(i);
                float walk_distance = 0;
                int station_number = 0;
                String station_start = null;
                String lineName = line.getName();
                String[] lineNames = lineName.split("\\|");
                String planInfo = "";
                for (int n = 0;n<lineNames.length;n++){
                    if(n!=lineNames.length-1){
                        planInfo += lineNames[n]+"---";
                    }else{
                        planInfo += lineNames[n];
                    }
                }
                map.put("time",line.getCostTime()+"分钟");
                map.put("plan",planInfo);
                data.add("");
                data.add("方案" + (i + 1));
                data.add("线路名称：" + line.getName());
                data.add("线路里程：" + line.getLength());
                data.add("线路用时：" + line.getCostTime());
                ArrayList<TTransitSegmentInfo> infos = line.getSegmentInfo();
                int flag = 0;
                for (int j = 0; j < infos.size(); j++) {
                    TTransitSegmentInfo info = infos.get(j);
                    data.add("");
                    data.add("路段" + (j + 1));
                    data.add("路段起点：" + info.getStart().getName());
                    data.add("路段终点：" + info.getEnd().getName());
                    String str = "" + info.getType();
                    int type = info.getType();
                    if (type == 1) {
                        str += "步行";
                    } else if (type == 2) {
                        str += "公交";
                    } else if (type == 3) {
                        str += "地铁";
                    } else {
                        str += "站内换乘";
                    }
                    data.add("路段类型：" + str);
                    if((flag==0) && (type!=1)){
                        station_start = info.getStart().getName();
                        flag++;
                    }
                    for (int k = 0; k < info.getSegmentLine().size(); k++) {
                        TTransitSegmentLine segmentLine = info.getSegmentLine().get(k);
                        if(type == 1){
                            walk_distance += segmentLine.getLength();
                        }else{
                            station_number += segmentLine.getStationCount();
                        }
//                        listPoints.add(segmentLine.getShapePoints());
                        for (int t =0;t<segmentLine.getShapePoints().size();t++){
                            GeoPoint point = segmentLine.getShapePoints().get(t);
                            if((j==infos.size()-1) && (t==segmentLine.getShapePoints().size()-1)){
                                points += point.getLatitudeE6()/ 1000000.0D + "," + point.getLongitudeE6()/ 1000000.0D;
                            }else{
                                points += point.getLatitudeE6()/ 1000000.0D + "," + point.getLongitudeE6()/ 1000000.0D + ";";
                            }
                        }
                        data.add("路段方案" + (k + 1) + "名称：" + segmentLine.getName());
                        data.add("路段方案" + (k + 1) + "方向：" + segmentLine.getDirection());
                        data.add("路段方案" + (k + 1) + "花费时间：" + segmentLine.getCostTime());
                        data.add("路段方案" + (k + 1) + "里程：" + segmentLine.getLength());
                        data.add("路段方案" + (k + 1) + "id：" + segmentLine.getId());
                        data.add("路段方案" + (k + 1) + "经过站数：" + segmentLine.getStationCount());
                    }
                }
                if(walk_distance>=1000) {
                    DecimalFormat decimalFormat=new DecimalFormat(".0");
                    map.put("distance", "步行" + decimalFormat.format(walk_distance/1000.0) + "公里");
                }else{
                    map.put("distance", "步行" + (int)walk_distance + "米");
                }
                map.put("station",station_number + "站" + "*" + station_start + "站上车");
                listData.add(map);
                listPoints.add(points);

            }
            changeFragment(listData,listPoints);
//        FragmentManager fm = pagerAdapter.fm;
//        List<Fragment> fragments = fm.getFragments();
//
//        FragmentTransaction transaction = fm.beginTransaction();
//        for (Fragment childFragment : fm.getFragments()) {
//            transaction.remove(childFragment);
//            transaction.commit();
//        }
//        pagerAdapter.removeALlFragments();
            pagerAdapter.updateList();
            pagerAdapter.setList(mFragmentList);
//        pagerAdapter.notifyDataSetChanged();
//        viewPager.setAdapter(pagerAdapter);//1.设置好ViewPager
//        viewPager.setCurrentItem(1);

        }

    }



    public class myFragmentPagerAdapter extends FragmentPagerAdapter {
            FragmentManager fm;
            final int PAGE_COUNT = 3;
            private String tabTitles[] = new String[]{"自驾", "公交", "步行"};
            private Context context;
            private List<PageFragment> fragmentList;
            private int[] updateflag = {0,0,0};

            public View getTabView(int position) {
                View v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
                TextView tv = (TextView) v.findViewById(R.id.textView);
                tv.setText(tabTitles[position]);
                tv.setGravity(Gravity.CENTER);
//            ImageView img = (ImageView) v.findViewById(R.id.imageView);
                //img.setImageResource(imageResId[position]);
                return v;
            }

            public myFragmentPagerAdapter(FragmentManager fm, Context context, List<PageFragment> fragmentList) {
                super(fm);
                this.fm = fm;
                this.context = context;
                this.fragmentList = fragmentList;
            }
//            private void removeALlFragments(){
//                FragmentTransaction transaction = fm.beginTransaction();
//                for (int i=0; i<fragmentList.size(); i++){
//                    Fragment fg = fragmentList.get(i);
//                    transaction.remove(fg);
//                }
//                transaction.commit();
//                fragmentList.clear();
//            }
            public void updateList(){
                updateflag[0] =1;
                updateflag[1] =1;
                updateflag[2] =1;
            }
            public void setList(List<PageFragment> list) {
//                FragmentTransaction transaction = fm.beginTransaction();
//                for (int i=0; i<fragmentList.size(); i++){
//                    Fragment fg = fragmentList.get(i);
//                    transaction.remove(fg);
//                    transaction.commit();
//                }
//
//                fragmentList.clear();
                this.fragmentList = list;
                notifyDataSetChanged();

            }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container,
                    position);
            if(updateflag[position]==1){

                String fragmentTag = fragment.getTag();
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(fragment);
                fragment = fragmentList.get(position);
                ft.add(container.getId(), fragment, fragmentTag);
                ft.attach(fragment);
                ft.commit();
                updateflag[position] = 0;

            }
            return fragment;
        }
            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

            @Override
            public int getCount() {
                return PAGE_COUNT;
            }

            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles[position];
            }
        }
    private void initFragment() {
        mFragmentList = new ArrayList<>();
        mFragmentList.add(PageFragment.newInstance(1,null,null));
        mFragmentList.add(PageFragment.newInstance(2,null,null));
        mFragmentList.add(PageFragment.newInstance(3,null,null));
    }
    private void changeFragment(List<Map<String, String>> listData,ArrayList<String> points) {
        mFragmentList = new ArrayList<>();
        mFragmentList.add(PageFragment.newInstance(1,null,null));
        mFragmentList.add(PageFragment.newInstance(2,listData,points));
        mFragmentList.add(PageFragment.newInstance(3,null,null));
    }
    /**
         * 默认适配器
         */
        public static class PageFragment extends Fragment {
            public static final String ARG_PAGE = "ARG_PAGE";
            public static final String ARG_DATA = "ARG_DATA";
//            public static final String ARG_TIME = "ARG_TIME";
//            public static final String ARG_DISTANCE = "ARG_DISTANCE";
//            public static final String ARG_PLAN = "ARG_PLAN";
//            public static final String ARG_STATION = "ARG_STATION";
            private int mPage;
            private List<Map<String, String>> mData;
            private ArrayList<String> mPoints;
//            private ArrayList<String>mTime;
//            private ArrayList<String> mDistance;
//            private ArrayList<String> mPlan;
//            private ArrayList<String> mStation;
            public static PageFragment newInstance(int page,@Nullable List<Map<String, String>> listData,@Nullable ArrayList<String> points) {
                Bundle args = new Bundle();
                args.putInt(ARG_PAGE, page);
                ArrayList data=new ArrayList();
                data.add(listData);
                data.add(points);
                args.putStringArrayList(ARG_DATA,data);
//                args.putStringArrayList(ARG_TIME,time);
//                args.putStringArrayList(ARG_DISTANCE,distance);
//                args.putStringArrayList(ARG_PLAN,plan);
//                args.putStringArrayList(ARG_STATION,station);
                PageFragment fragment = new PageFragment();
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                mPage = getArguments().getInt(ARG_PAGE);
                ArrayList data = getArguments().getStringArrayList(ARG_DATA);
                mData = (List<Map<String, String>>)data.get(0);
                mPoints = (ArrayList<String>)data.get(1);
//                mTime = getArguments().getStringArrayList(ARG_TIME);
//                mDistance = getArguments().getStringArrayList(ARG_DISTANCE);
//                mPlan = getArguments().getStringArrayList(ARG_PLAN);
//                mStation = getArguments().getStringArrayList(ARG_STATION);

            }

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
                View view;
                if(mData!=null){
                    view = inflater.inflate(R.layout.fragment_page, container, false);
                    ListView listView = (ListView) view;
                    listView.setAdapter((ListAdapter) new SimpleAdapter(getActivity(),mData,R.layout.list_item,
                            new String[]{"time","distance","plan","station"},new int[]{R.id.time,R.id.distance,R.id.plan,R.id.station}));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), DetailrouteActivty.class);
//                            SerializableList myList=new SerializableList();
//                            myList.setList(mPoints);//将map数据添加到封装的myMap中

//                            bundle.putSerializable("points", myList);
                            intent.putExtra("points",mPoints.get(position));
                            startActivity(intent);


                        }
                    });

                }else{
                    view = inflater.inflate(R.layout.fragment_page0, container, false);
                    TextView textView = (TextView) view;


                    textView.setText("Fragment #" + mPage);
                }

                return view;
            }
    }



}
