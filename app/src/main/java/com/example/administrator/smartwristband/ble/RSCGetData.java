package com.example.administrator.smartwristband.ble;

import android.util.Log;

public class RSCGetData extends BaseBleMessage {

    public static String ACTION_GET_RSC_DATA = "com.example.wristband.rsc.ACTION_GET_RSC_DATA";
    public static byte mOperateCode = 0;
    public static byte mTheCmd = 6;
    private int cal;
    private int distince;
    private int speed;
    private int steps;

    //解析计步数据
    public int dealBleResponse(byte[] paramArrayOfByte, int paramInt) {
        this.steps = 0;
        this.distince = 0;
        this.cal = 0;
        this.speed = 0;
        Log.d("RSCGetData", "dealBleResponse");

        //数据长度异常，返回1
        if ((paramInt < 15) && (paramInt > 1000)) {
            return 1;
        }

        //获取计步
        this.steps += (paramArrayOfByte[2] & 0xFF);
        this.steps += (paramArrayOfByte[3] << 8 & 0xFFFF);
        this.steps += (paramArrayOfByte[4] << 16 & 0xFFFFFF);
        this.steps += (paramArrayOfByte[5] << 32 & 0xFFFFFFFF);

        //获取距离
        this.distince += (paramArrayOfByte[6] & 0xFF);
        this.distince += (paramArrayOfByte[7] << 8 & 0xFFFF);
        this.distince += (paramArrayOfByte[8] << 16 & 0xFFFFFF);
        this.distince += (paramArrayOfByte[9] << 32 & 0xFFFFFFFF);

        //获取卡路里
        this.cal += (paramArrayOfByte[10] & 0xFF);
        this.cal += (paramArrayOfByte[11] << 8 & 0xFFFF);
        this.cal += (paramArrayOfByte[12] << 16 & 0xFFFFFF);
        this.cal += (paramArrayOfByte[13] << 32 & 0xFFFFFFFF);

        //获取步速
        this.speed = (paramArrayOfByte[14] & 0xFF);
        //数据解析成功，返回0
        return 0;
    }

    public int getCal() {
        return this.cal;
    }


    //设置获取步数的指令
    public byte[] getData() {
        //发送计步的指令 --68 06 01 00 00 6f 16
        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 0;
        return setMessageByteData(mTheCmd, arrayOfByte, arrayOfByte.length);
    }

    public int getDistince() {
        return this.distince;
    }

    public int getSpeed() {
        return this.speed;
    }

    public int getSteps() {
        return this.steps;
    }

    //关闭计步测试
    public byte[] offRSC() {

        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 2;
        return setMessageByteData(mTheCmd, arrayOfByte, arrayOfByte.length);
    }

    //打开计步测试
    public byte[] onRSC() {
        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 1;
        return setMessageByteData(mTheCmd, arrayOfByte, arrayOfByte.length);
    }
}
