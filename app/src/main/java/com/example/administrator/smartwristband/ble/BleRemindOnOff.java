package com.example.administrator.smartwristband.ble;

import android.content.IntentFilter;
import android.util.Log;

public class BleRemindOnOff extends BaseBleMessage {

    public static final int REMIND_TYPE_LOST = 1;
    public static final int REMIND_TYPE_PHONE = 3;
    public static final int REMIND_TYPE_SMS = 2;
    public static final String TAG = "tixing";
    public static final byte mTheCmd = 5;

    //对返回的指令进行处理
    public byte[] dealBleResponse(byte[] notifyData, int dataLen) {
        boolean bool = true;
        if (dataLen <= 2) {
            return null;
        }
        int type = notifyData[1];
        boolean isOpened = (notifyData[2] == 0x01) ? true : false;

        switch (type) {
            case REMIND_TYPE_LOST:
                Log.d(TAG, "防丢状态：" + isOpened);
                break;
            case REMIND_TYPE_SMS:
                Log.d(TAG, "短信状态：" + isOpened);
                break;
            case REMIND_TYPE_PHONE:
                Log.d(TAG, "电话：" + isOpened);
                break;
        }
        return null;
    }

    public byte[] readRemindStatus(int type) {
        byte[] data = new byte[2];
        data[0] = 0x01;
        switch (type) {
            case REMIND_TYPE_LOST:
                data[1] = 0x01;
                break;
            case REMIND_TYPE_SMS:
                data[1] = 0x02;
                break;
            case REMIND_TYPE_PHONE:
                data[1] = 0x03;
                break;
        }
        return setMessageByteData(mTheCmd, data, data.length);
    }

    public byte[] switchRemind(int remindType, boolean remindOnOff) {
        byte[] data = new byte[3];
        data[0] = 0x00;
        switch (remindType) {
            case REMIND_TYPE_LOST:
                data[1] = 0x01;
                data[2] = (byte) (remindOnOff ? 0x01 : 0x00);
                return setMessageByteData(mTheCmd, data, data.length);
            case REMIND_TYPE_SMS:
                data[1] = 0x02;
                return setMessageByteData(mTheCmd, data, data.length);

            case REMIND_TYPE_PHONE:
                data[1] = 0x03;
                return setMessageByteData(mTheCmd, data, data.length);
        }
        return null;
    }
}
