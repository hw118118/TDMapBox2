package com.example.hgis.tdmapbox2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by HGIS on 2017/8/11.
 */

public class MySettingActivity extends Activity implements View.OnClickListener{
    private TextView user_name;
    private ImageView header_img;//登录按钮
    private TextView mml_msg_tv;//设置
    private TextView mml_feedback_tv;//反馈
    private TextView tv_about;//关于
    private TextView mml_checkupdate_tv;//检查更新
    private TextView offlinemap_tv;//离线地图
    private TextView loginout_tv;//退出登录
    private Context mContext;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mysetting);
        initView();
        addListener();
    }
    private void initView(){
        user_name=findViewById(R.id.user_name);
        mContext=this;
        header_img=findViewById(R.id.header_img);
        //设置
        mml_msg_tv=findViewById(R.id.mml_msg_tv);
        //反馈
        mml_feedback_tv=findViewById(R.id.mml_feedback_tv);
        //关于
        tv_about=findViewById(R.id.tv_about);
        //检查更新
        mml_checkupdate_tv=findViewById(R.id.mml_checkupdate_tv);
        //离线地图
        offlinemap_tv=findViewById(R.id.offlinemap_tv);
        //退出登录
        loginout_tv=findViewById(R.id.loginout_tv);
    }
    private void addListener(){
        header_img.setOnClickListener(this);
        mml_msg_tv.setOnClickListener(this);
        mml_feedback_tv.setOnClickListener(this);
        tv_about.setOnClickListener(this);
        mml_checkupdate_tv.setOnClickListener(this);
        offlinemap_tv.setOnClickListener(this);
        loginout_tv.setOnClickListener(this);
    }
    /**
     * 通过startActivityForresult跳转  接收返回的数据
     * @param requestCode  请求的标识
     * @param resultCode   第二个页面返回的标识
     * @param data         第二个页面传回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1&&resultCode==2){
            String result=data.getStringExtra("data");
            user_name=findViewById(R.id.user_name);
            user_name.setText(result);
            header_img.setImageResource(R.mipmap.user_online);
            header_img.setOnClickListener(null);
        }

    }
    private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder){
        return builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user_name.setText("登陆/注册");
                header_img.setImageResource(R.mipmap.user_offline);
                header_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //初始化Intent
                        /**
                         * 第一个参数 上下文对象this
                         * 第二个参数 目标文件
                         */
                        Intent intent=new Intent(mContext,LoginActivity.class);
                        /**
                         * 第一个参数Intent对象
                         * 第二个参数 请求标志
                         */
                        startActivityForResult(intent,1);
                    }
                });
            }
        });
    }
    private AlertDialog.Builder setNegativeButton(AlertDialog.Builder builder){
        return builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.header_img:
                //初始化Intent
                /**
                 * 第一个参数 上下文对象this
                 * 第二个参数 目标文件
                 */
                Intent intent=new Intent(mContext,LoginActivity.class);
                /**
                 * 第一个参数Intent对象
                 * 第二个参数 请求标志
                 */
                startActivityForResult(intent,1);
                break;
            case R.id.mml_msg_tv:
                startActivity(new Intent(MySettingActivity.this,MsgActivity.class));
                break;
            case R.id.mml_feedback_tv:
                if(!user_name.getText().equals("登陆/注册")) {
                    Intent feedback_intent = new Intent(mContext, FeedBackActivity.class);
                    feedback_intent.putExtra("username", user_name.getText());
                    startActivity(feedback_intent);
                }else {
                    Toast.makeText(MySettingActivity.this,"请先登录",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.tv_about:
                startActivity(new Intent(mContext,AboutActivity.class));
                break;
            case R.id.mml_checkupdate_tv:
                Toast.makeText(MySettingActivity.this,"当前已是最新版本",Toast.LENGTH_LONG).show();
                break;
            case R.id.offlinemap_tv:
                startActivity(new Intent(MySettingActivity.this,OfflineManagerActivity.class));
                break;
            case R.id.loginout_tv:
                if(!user_name.getText().equals("登陆/注册")) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(MySettingActivity.this);
                    builder.setMessage("是否退出登录");
                    setPositiveButton(builder);
                    setNegativeButton(builder);
                    builder.create();
                    builder.show();
                }else {
                    Toast.makeText(MySettingActivity.this,"您还没有登录",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
