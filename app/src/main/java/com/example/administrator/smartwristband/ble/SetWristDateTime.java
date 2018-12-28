package com.example.administrator.smartwristband.ble;

import android.util.Log;

public class SetWristDateTime extends BaseBleMessage {
    private static String TAG = "SetWristDateTime";
    public static final byte mTheCmd = 32;

    //   <<      :     左移运算符，num << 1,相当于num乘以2
    //   >>      :     右移运算符，num >> 1,相当于num除以2
    //   >>>    :     无符号右移，忽略符号位，空位都以0补齐

    public static byte[] longToByteArray(long longdata) {
        byte[] arrayOfByte = new byte[4];
        int i = 0;
        while (i < 4) {
            arrayOfByte[i] = ((byte) (int) (longdata >>> (arrayOfByte.length - 1 - i) * 8 & 0xFF));
            i += 1;
        }
        return arrayOfByte;
    }

    //发送指令
    public byte[] sendDatetowrister() {
        long l = System.currentTimeMillis() / 1000L + 28800L;
        byte[] arrayOfByte1 = longToByteArray(l);
        int i = 0;
        Log.e(TAG, BaseBleMessage.byteArrToString(arrayOfByte1) + "  " + l);
        if (arrayOfByte1 != null) {
            i = arrayOfByte1.length;
        }
        byte[] arrayOfByte2 = new byte[i];
        int j = 0;
        while (j < i) {
            arrayOfByte2[j] = arrayOfByte1[(i - 1 - j)];
            j += 1;
        }
        Log.e(TAG, "data=" + BaseBleMessage.byteArrToString(arrayOfByte1));

        return setMessageByteData((byte) 32, arrayOfByte2, arrayOfByte2.length);
    }


}
