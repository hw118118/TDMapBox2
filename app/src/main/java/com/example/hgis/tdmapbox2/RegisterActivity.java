package com.example.hgis.tdmapbox2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hgis.tdmapbox2.com.tools.Utils;
import com.example.hgis.tdmapbox2.com.web.WebService;
import com.mapbox.services.commons.utils.TextUtils;

/**
 * Created by HGIS on 2017/8/15.
 */

public class RegisterActivity extends Activity implements View.OnClickListener{
    private ImageView iv_back;
    private Button btn_regist;
    private EditText etMobile;
    private EditText etPwd;
    // 创建等待框
    private ProgressDialog dialog;
    // 返回的数据
    private String info;
    // 返回主线程更新数据
    private static Handler handler = new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        initView();
        addListener();
    }
    private void initView(){
        iv_back=findViewById(R.id.iv_back);
        btn_regist=findViewById(R.id.btn_regist);
        etMobile=findViewById(R.id.et_mobile);
        etPwd=findViewById(R.id.et_pwd);
    }
    private void addListener(){
        iv_back.setOnClickListener(this);
        btn_regist.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_regist:
                if (!Utils.checkNetwork(this)){
                    Toast toast=Toast.makeText(this,"网络未连接",Toast.LENGTH_LONG);
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
                dialog=new ProgressDialog(this);
                dialog.setTitle("提示");
                dialog.setMessage("正在注册,请稍后...");
                dialog.setCancelable(false);
                dialog.show();
                new Thread(new RegisterActivity.MyThread()).start();
                break;
        }
    }
    //子线程接受数据  主线程修改数据
    public class MyThread implements Runnable{
        @Override
        public void run() {
//            info= WebServicePost.executeHttpPost(etMobile.getText().toString(),etPwd.getText().toString(),"RegLet");
            info= WebService.executeHttpGet(etMobile.getText().toString(),etPwd.getText().toString(),"RegLet");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i("tag",info);
                    if(info.matches("[0-9]+")){
                        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_LONG).show();
                        Intent data=new Intent();
                        data.putExtra("data",info);
                        setResult(2,data);
                        //结束当前页面
                        finish();
                    }else{
                        Toast.makeText(RegisterActivity.this,info,Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                }
            });
        }
    }
}
