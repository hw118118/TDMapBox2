package com.example.hgis.tdmapbox2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hgis.tdmapbox2.com.web.WebService;

/**
 * Created by HGIS on 2017/8/16.
 */

public class FeedBackActivity extends Activity implements View.OnClickListener{
    private Button btn_submit;
    private ImageView iv_back;
    private EditText editText;
    private String username;
    //创建等待框
    private ProgressDialog dialog;
    //返回的数据
    private String info;
    //返回主线程更新数据
    private static Handler handler=new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_back);
        initView();
        addListener();
    }
    private void initView(){
        btn_submit=findViewById(R.id.btn_submit);
        iv_back=findViewById(R.id.iv_back);
        editText=findViewById(R.id.et_content);
        username=this.getIntent().getStringExtra("username");
    }
    private void addListener(){
        btn_submit.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_submit:
                String msg=editText.getText().toString().trim();
                if(msg.length()==0){
                    Toast.makeText(this,"请输入反馈内容",Toast.LENGTH_LONG).show();
                    return;
                }
                dialog=new ProgressDialog(this);
                dialog.setTitle("提示");
                dialog.setMessage("正在提交反馈信息,请稍后...");
                dialog.setCancelable(false);
                dialog.show();
                //创建子线程,分别进行Get和Post
                new Thread(new FeedBackActivity.MyThread()).start();
                break;

        }
    }
    //子线程接受数据  主线程修改数据
    public class MyThread implements Runnable{
        @Override
        public void run() {
            info= WebService.executeHttpPost("15951706978",editText.getText().toString(),"GetMsg");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (!info.equals("反馈失败")){
                        editText.setText("");
                    }
                    Toast.makeText(FeedBackActivity.this,info,Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        }
    }
}
