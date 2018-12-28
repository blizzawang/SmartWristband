package com.example.administrator.smartwristband.ble;

public class HRSgetData extends BaseBleMessage {

    public static String ACTION_CLOSE_HRS = "cn.example.wristband.rsc.ACTION_CLOSE_HRS";
    public static String ACTION_GET_HRS_DATA;
    public static String ACTION_OPEN_HRS;
    public static String ACTION_RECEIVER_HRS = "cn.example.wristband.rsc.ACTION_RECEIVER_HRS";
    public static byte mOperateCode = 0;
    public static byte mTheCmd = 6;
    private int heartrate;

    static {
        ACTION_OPEN_HRS = "cn.example.wristband.rsc.ACTION_OPEN_HRS";
        ACTION_GET_HRS_DATA = "com.example.wristband.rsc.ACTION_GET_RSC_DATA";
    }

    //解析返回的数据
    public int dealBleResponse(byte[] paramArrayOfByte, int paramInt) {
        if ((paramInt < 15) && (paramInt > 1000)) {
            return 1;
        }
        this.heartrate = (paramArrayOfByte[1] & 0xFF);
        return 0;
    }

    public int getHRS() {
        return this.heartrate;
    }

    //发送获取心跳的指令
    public byte[] getHRSData() {
        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 0;
        return setMessageByteData(mTheCmd, arrayOfByte, arrayOfByte.length);
    }

    public byte[] offHRS() {
        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 2;
        return setMessageByteData(mTheCmd, arrayOfByte, arrayOfByte.length);
    }

    public byte[] onHRS() {
        //发送计步的指令 --68 06 01 00 00 6f 16
        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 0;
        return setMessageByteData(mTheCmd, arrayOfByte, arrayOfByte.length);
    }
}
