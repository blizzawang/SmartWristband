package com.example.administrator.smartwristband.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SpInfo {

    // 保存用户名和密码和登录状态
    public static void SaveInfo(Context context, String userName, String psw, boolean isLogin) {
        String Md5Psw = MD5Utils.md5Utils(psw);
        SharedPreferences sp = context.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        edit.putString("username", userName);
        edit.putString("pwd", Md5Psw);
        edit.putBoolean("isLogin", isLogin);
        edit.commit();
    }

    // 获取是SP中保存的用户名和密码
    public static Map<String, String> getInfo(Context context) {
        Map<String, String> maps = new HashMap<String, String>();
        SharedPreferences sp = context.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        String name = sp.getString("username", null);
        String pwd = sp.getString("pwd", null);

        maps.put("username", name);
        maps.put("pwd", pwd);
        return maps;
    }

    // 判断登录状态
    public static boolean readLoginStatus(Context context) {
        // TODO Auto-generated method stub
        SharedPreferences sp = context.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        boolean islogin = sp.getBoolean("isLogin", false);
        return islogin;
    }

    // 修改登录状态
    public static void SaveLoginInfo(Context context, boolean isLogin) {
        SharedPreferences sp = context.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        edit.putBoolean("isLogin", isLogin);
        edit.commit();
    }

    //判断时候第一次登陆
    public static boolean getBoolean(Context context, String key, boolean defvalue) {
        SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        boolean aBoolean = preferences.getBoolean(key, defvalue);
        return aBoolean;
    }

    //设置第一次启动的状态
    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(key, value).commit();
    }


}
