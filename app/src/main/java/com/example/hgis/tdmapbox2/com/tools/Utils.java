package com.example.hgis.tdmapbox2.com.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by HGIS on 2017/8/23.
 */
public class Utils {
    public  static boolean isEmail(String email){
        String str="^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }
    public  static boolean isMobile(String phone){
        Pattern p = Pattern.compile("^((14[0-9])|(13[0-9])|(15[0-9])|(18[0-9])|(17[0-9]))\\d{8}$");
        Matcher m = p.matcher(phone);
        return m.matches();
    }
    public static boolean checkNetwork(Context context){
        ConnectivityManager connManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
}
