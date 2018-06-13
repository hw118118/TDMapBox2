package com.example.hgis.tdmapbox2.com.web;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HGIS on 2017/8/22.
 */

public class WebService {
    /**
     * static变量也称作静态变量，静态变量和非静态变量的区别是：静态变量被所有的对象所共享，
     * 在内存中只有一个副本，它当且仅当在类初次加载时会被初始化。
     * 而非静态变量是对象所拥有的，在创建对象的时候被初始化，存在多个副本，各个对象拥有的副本互不影响。
     */
    private static String IP="10.5.211.23:8080";
    //通过Get方式获取HTTP服务器
    public static String executeHttpGet(String username,String password,String address){

        HttpURLConnection conn = null;
        InputStream is = null;
        try{
            String path="http://"+IP+"/tdmapserver/"+address;
            path=path+"?username="+username+"&password="+password;
            Log.i("tag",path);
            conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setConnectTimeout(5000);//设置超时时间
            conn.setReadTimeout(5000);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");//设置获取信息方式
            conn.setRequestProperty("Charset","UTF-8");

            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                return parseInfo(is);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //意外退出时进行连接关闭保护
            if (conn!=null){
                conn.disconnect();
            }
            if(is!=null){
                try{
                    is.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return "服务器连接超时...";
    }

    public static  String executeHttpPost(String username,String msg,String address){
        try{
            String path="http://"+IP+"/tdmapserver/"+address;
            //发送指令和信息
            Map<String,String> params=new HashMap<String,String>();
            params.put("username",username);
            params.put("msg",msg);

            return sendPOSTRequest(path,params,"UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        return "服务器连接超时...";
    }
    //将输入流转化为String型
    private static String parseInfo(InputStream inputStream) throws Exception {
        byte[] data=read(inputStream);
        //转化为字符串
        return new String(data,"utf-8");
    }
    public static byte[] read(InputStream inputStream) throws Exception{
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        byte[] buffer=new byte[1024];
        int len=0;
        while ((len=inputStream.read(buffer))!=-1){
            outputStream.write(buffer,0,len);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }

    //处理发送数据请求
    private static String sendPOSTRequest(String path,Map<String,String> params,String encoding) throws Exception{
        List<NameValuePair> pairs=new ArrayList<NameValuePair>();
        if(params!=null&&!params.isEmpty()){
            for(Map.Entry<String,String> enty:params.entrySet()){
                pairs.add(new BasicNameValuePair(enty.getKey(),enty.getValue()));
            }
        }
        UrlEncodedFormEntity entity=new UrlEncodedFormEntity(pairs,encoding);
        HttpPost post=new HttpPost(path);
        post.setEntity(entity);
        DefaultHttpClient client=new DefaultHttpClient();
        //请求超时
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,5000);
        //读取超时
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,5000);
        HttpResponse response=client.execute(post);

        //判断是否成功收取消息
        if (response.getStatusLine().getStatusCode()==200){
            return getInfo(response);
        }
        //未收到信息  返回空指针
        return "请求失败";
    }
    //收取数据
    private static String getInfo(HttpResponse response) throws Exception{
        HttpEntity entity=response.getEntity();
        InputStream is=entity.getContent();
        //将输入流转化为byte型
        byte[] data=WebService.read(is);
        //转化为字符串
        return new String(data,"UTF-8");
    }
}
