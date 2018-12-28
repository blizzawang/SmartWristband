package com.example.administrator.smartwristband.ble;

import android.util.Log;

public abstract class BaseBleMessage {

    // 68    XX      XXXX        XX        XX         16
    //包头  功能码   数据长度    数据      检验码     尾帧


    public static final String BASE_TAG = "BLE_COM";
    private byte msg_head = 104;//104 十六进制0x68
    private byte msg_cmd = 0x00;
    private byte[] msg_data;
    private int msg_data_len = 0;
    private byte msg_tail = 22;//22 十六进制0x16



    //十六进制数字转化成字符串
    public static String byteArrToString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xff);
            if (hex.length() % 2 == 1) {
                hex = "0" + hex;
            }
            result += hex;
        }
        return result.toUpperCase();
    }

    //生成校验码
    public byte calS(byte[] value, int len) {
        int i = 0;
        int cs = 0;
        for (int j = 0; j < len; j++) {
            cs = cs + value[j];
        }
        return (byte) (cs & 0xff);
    }

    //发送指令码 --paramByte-功能码   paramArrayOfByte--发送指令的数据  --paramInt指令长度
    public byte[] setMessageByteData(byte paramByte, byte[] paramArrayOfByte, int paramInt) {
        this.msg_cmd = paramByte;
        this.msg_data_len = paramInt;
        this.msg_data = paramArrayOfByte;
        return getSendByteArray();
    }


    //构建发送的指令码
    public byte[] getSendByteArray() {
        //除了 指令本身的长度之外，还需要6个byte: 包头（1） 数据长度(2)  校验码(1)  包尾(1)
        byte[] arrayOfByte = new byte[this.msg_data_len + 6];

        //第一个字节-包头
        arrayOfByte[0] = this.msg_head;
        //第二个字节-功能码
        arrayOfByte[1] = this.msg_cmd;
        //第二个字节-取int类型(msg_data_len)的后八位bit 放在前面
        arrayOfByte[2] = ((byte) (this.msg_data_len & 0xFF));
        //第三个字节-取int类型数据的9-16位bit 放在后面
        arrayOfByte[3] = ((byte) (this.msg_data_len >> 8 & 0xFF));
        //如果发送的数据长度不为0 并且有数据
        if ((this.msg_data_len != 0) && (this.msg_data != null)) {
            //循环取数据并赋值
            int i = 0;
            while (i < this.msg_data_len) {
                //分别取数据，并赋值给响应的byte位
                arrayOfByte[(i + 4)] = this.msg_data[i];
                i += 1;
            }
        }
        //倒数第二位 是校验码--由 calS()函数构成校验码
        arrayOfByte[(this.msg_data_len + 4)] = calS(arrayOfByte, this.msg_data_len + 4);
        //倒数第一位 是包尾
        arrayOfByte[(this.msg_data_len + 5)] = this.msg_tail;
        Log.e("BLE_COM", "send: " + byteArrToString(arrayOfByte));
        return arrayOfByte;
    }
}
