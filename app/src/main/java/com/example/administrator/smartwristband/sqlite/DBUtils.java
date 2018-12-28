package com.example.administrator.smartwristband.sqlite;

import java.sql.Blob;
import java.sql.SQLException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.smartwristband.bean.UserBean;

public class DBUtils {
    private static SQLiteHelper helper;
    private static SQLiteDatabase db;
    private static DBUtils instance = null;
    private byte[] bs;

    public DBUtils(Context context) {
        helper = new SQLiteHelper(context);
        db = helper.getWritableDatabase();
    }

    public static DBUtils getInstance(Context context) {
        // 单例模式
        if (instance == null) {
            instance = new DBUtils(context);
        }
        return instance;
    }

    // 保存个人信息
    public void saveUserInfo(UserBean user) {
        ContentValues values = new ContentValues();
        values.put("username", user.getUserName());
        values.put("nickName", user.getNickName());
        values.put("sex", user.getSex());
        values.put("signature", user.getSignature());
        values.put("image", user.getImages());
        values.put("pwd", user.getPwd());
        values.put("age", user.getAge());
        values.put("age", user.getPhone());
        db.insert("userinfo", null, values);

    }

    // 将图片转成byte[]类型
    private byte[] getPicture(Blob image) {
        try {
            if (image != null) {
                byte[] buffer = image.getBytes(1, (int) image.length());
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    // 获取个人信息
    public UserBean getUserInfo(String username) {
        UserBean user = null;
        Cursor cursor = db.query("userinfo", null, "username=?", new String[]{username}, null, null, null);
        if (cursor.getCount() > 0 && cursor != null) {
            if (cursor.moveToNext()) {
                user = new UserBean();
                user.setUserName(cursor.getString(cursor.getColumnIndex("username")));
                user.setNickName(cursor.getString(cursor.getColumnIndex("nickName")));
                user.setSex(cursor.getString(cursor.getColumnIndex("sex")));
                user.setSignature(cursor.getString(cursor.getColumnIndex("signature")));
                user.setImages(cursor.getString(cursor.getColumnIndex("image")));
                user.setPwd(cursor.getString(cursor.getColumnIndex("pwd")));
                user.setSecurity(cursor.getString(cursor.getColumnIndex("security")));
                user.setAge(cursor.getInt(cursor.getColumnIndex("age")));
                user.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
            }
            cursor.close();
            return user;
        } else {
            return null;
        }

    }

    // 修改个人信息
    public void updateUserInfo(String key, String value, String userName) {
        ContentValues cv = new ContentValues();
        cv.put(key, value);
        db.update("userinfo", cv, "userName=?", new String[]{userName});
    }

    // 获取头像
    public byte[] getImage(String username) {
        Cursor cursor = db.query("userinfo", new String[]{"image"}, "username=?", new String[]{username}, null, null, null);
        if (cursor.getCount() > 0 && cursor != null) {
            if (cursor.moveToNext()) {
                bs = cursor.getBlob(cursor.getColumnIndex("image"));
            }
        }
        cursor.close();
        return bs;
    }


}
