package com.example.hgis.tdmapbox2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.hgis.tdmapbox2.com.tools.Utils;
import com.example.hgis.tdmapbox2.com.web.WebService;
import com.mapbox.services.commons.utils.TextUtils;

/**
 * Created by HGIS on 2017/8/14.
 */

public class LoginActivity  extends Activity implements View.OnClickListener{
    private ImageView iv_back;//返回用户设置页面按钮
    private Button btn_login;//登陆
    private TextView tv_regist;//注册
    private EditText etMobile;
    private EditText etPwd;
    private TextView tv_forgetpwd;
    //创建等待框
    private ProgressDialog dialog;
    //返回的数据
    private String info;
    //返回主线程更新数据
    private static Handler handler=new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initView();
        addListener();
    }
    private void initView(){
        iv_back=findViewById(R.id.iv_back);
        etMobile=findViewById(R.id.et_mobile);
        etPwd=findViewById(R.id.et_pwd);
        btn_login=findViewById(R.id.btn_login);

        tv_forgetpwd=findViewById(R.id.tv_forget_pwd);
        tv_regist=findViewById(R.id.tv_regist);
    }
    private void addListener(){
        iv_back.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        tv_forgetpwd.setOnClickListener(this);
        tv_regist.setOnClickListener(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1&&resultCode==2){
            String result=data.getStringExtra("data");
            etMobile.setText(result);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                //监测网络  无法监测wifi
                if(!Utils.checkNetwork(this)){
                    Toast toast=Toast.makeText(LoginActivity.this,"网络未连接",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                    break;
                }
                String mobile=etMobile.getText().toString().trim();
                String pwd=etPwd.getText().toString().trim();
                if(TextUtils.isEmpty(mobile)){
                    Toast.makeText(this,"请输入手机号",Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)){
                    Toast.makeText(this,"请输入密码",Toast.LENGTH_LONG).show();
                    return;
                }
                if(!Utils.isMobile(mobile)){
                    Toast.makeText(this,"输入的电话号码不合格",Toast.LENGTH_LONG).show();
                    return;
                }
//                if(!Utils.isMobile(mobile)){
//                    Toast.makeText(this,"输入的手机号有误",Toast.LENGTH_LONG).show();
//                    return;
//                }
                //提示框
                dialog=new ProgressDialog(this);
                dialog.setTitle("提示");
                dialog.setMessage("正在登陆,请稍后...");
                dialog.setCancelable(false);
                dialog.show();
                //创建子线程,分别进行Get和Post
                new Thread(new MyThread()).start();
                break;
            case R.id.tv_regist:
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivityForResult(intent,1);
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }
    //子线程接受数据  主线程修改数据
    public class MyThread implements Runnable{
        @Override
        public void run() {
            info= WebService.executeHttpGet(etMobile.getText().toString(),etPwd.getText().toString(),"LogLet");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(info.matches("[0-9]+")){
                        Intent data=new Intent();
                        data.putExtra("data",info);
                        setResult(2,data);
                        //结束当前页面
                        finish();
                    }else{
                        Toast.makeText(LoginActivity.this,info,Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                }
            });
        }
    }
}