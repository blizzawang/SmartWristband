package com.example.administrator.smartwristband.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.R.integer;

public class MD5Utils {

    /*
     * MD5加密算法
     */
    public static String md5Utils(String text) {
        MessageDigest digest = null;
        try {
            // // 拿到一个MD5转换器
            digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(text.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte b : result) {
                int number = b & 0Xff;
                // java.lang.Integer.toHexString() 方法返回为无符号整数基数为16的整数参数的字符串表示形式。
                // 以下字符作为十六进制数字：0123456789ABCDEF。
                String hex = Integer.toHexString(number);
                if (hex.length() == 1) {

                    sb.append("0" + hex);
                } else {
                    sb.append(hex);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }
}
