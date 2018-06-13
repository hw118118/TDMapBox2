package com.example.hgis.tdmapbox2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hgis.tdmapbox2.com.RouteActivity;
import com.mapbox.services.commons.utils.TextUtils;

import java.lang.reflect.Field;

/**
 * Created by HGIS on 2017/8/18.
 */

public class NearbyActivity extends Activity {
    private SearchView nearby_sv;
    private TextView more;
    private double startLat;
    private double startLon;
    //自动完成的列表
    private final String[] mStrings={"武汉大学","长江大桥"};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby);

        nearby_sv=findViewById(R.id.nearby_sv);
        //设置该nearby_sv默认是否自动缩小为图标
        nearby_sv.setIconifiedByDefault(false);
        //设置该nearby_sv显示搜索按钮
        nearby_sv.setSubmitButtonEnabled(false);
        //设置该nearby_sv内默认显示的提示文本
        nearby_sv.setQueryHint("搜索附近");
        if (nearby_sv!=null){
            try{
                //拿到字节码
                Class<?> argclass=nearby_sv.getClass();
                Field field=argclass.getDeclaredField("mSearchPlate");
                field.setAccessible(true);
                View view= (View) field.get(nearby_sv);
                view.setBackgroundResource(R.drawable.nearby);
            }catch (Exception e){
                e.printStackTrace();

            }
        }
        //为该nearby_sv组件设置时间监听器
        nearby_sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //单机搜索按钮时激发该方法
            @Override
            public boolean onQueryTextSubmit(String s) {
                Toast.makeText(NearbyActivity.this,"您输入的内容是"+s,Toast.LENGTH_LONG).show();
                return false;
            }
            //用户输入字符时激发该方法
            @Override
            public boolean onQueryTextChange(String s) {

                return true;
            }
        });
        //返回上一级
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //结束当前页面
                finish();
            }
        });
        more  = (TextView)findViewById(R.id.tv_more);
        Intent lastintent = getIntent();
        Bundle bundle = lastintent.getExtras();
        startLat  = bundle.getDouble("lat");
        startLon  = bundle.getDouble("lon");
        more.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(NearbyActivity.this, RouteActivity.class);
                intent.putExtra("lat",startLat);
                intent.putExtra("lon",startLon);
                startActivity(intent);
            }


        });
    }
}
