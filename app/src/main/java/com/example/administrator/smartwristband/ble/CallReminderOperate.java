package com.example.administrator.smartwristband.ble;

import java.io.UnsupportedEncodingException;

public class CallReminderOperate extends BaseBleMessage {

    //功能码
    public static final byte mTheCmd = 1;
    public static final byte mTheCmdDelay = 18;
    private byte[] parambyte;
    private byte[] arrayOfByte;
    private byte[] nameByte;


    //关闭指令
    public byte[] closeCallReminder() {
        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 1;
        return setMessageByteData((byte) 1, arrayOfByte, arrayOfByte.length);
    }

    //发送电话提醒指令
    public byte[] sendReminder(String paramString, String nameString) {

        int len = 0;
        if (nameString != null) {
            try {
                parambyte = nameString.getBytes("UTF-8");
                System.out.println("名字转化成byte:" + BaseBleMessage.byteArrToString(parambyte));
                len = parambyte.length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        arrayOfByte = new byte[16 + len];

        int i = 0;
        while (i < (16 + len)) {
            arrayOfByte[i] = 0;
            i += 1;
        }

        //处理电话号码和姓名
        if (paramString != null) {
            try {
                //把字符串转化成byte数组
                byte[] numberByte = paramString.getBytes("US-ASCII");
                if (numberByte != null) {
                    i = 0;
                    while (i < numberByte.length) {
                        arrayOfByte[(i + 1)] = numberByte[i];
                        i += 1;
                    }
                }
                if (nameString != null) {
                    nameByte = nameString.getBytes("UTF-8");
                    if (nameByte != null) {
                        i = 16;
                        int end = i + nameByte.length;
                        while (i < end) {
                            arrayOfByte[i] = nameByte[i - 16];
                            i += 1;
                        }
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.println("电话和姓名:" + BaseBleMessage.byteArrToString(arrayOfByte));
        return setMessageByteData((byte) 1, arrayOfByte, arrayOfByte.length);
    }

    public byte[] senddelayreminder(String paramString) {
        byte[] arrayOfByte = new byte[2];
        arrayOfByte[0] = 1;
        arrayOfByte[1] = Byte.valueOf(paramString).byteValue();
        return setMessageByteData((byte) 1, arrayOfByte, arrayOfByte.length);
    }

    //打开来电提醒的命令
    public byte[] sendOpenreminder() {
        byte[] bytes = new byte[3];
        bytes[0] = 0x00;
        bytes[1] = 0x03;
        bytes[2] = 0x01;
        return setMessageByteData((byte) 5, bytes, bytes.length);
    }

    //打开短信提醒的命令
    public byte[] sendOpenMessagereminder() {
        byte[] bytes = new byte[3];
        bytes[0] = 0x00;
        bytes[1] = 0x02;
        bytes[2] = 0x01;
        return setMessageByteData((byte) 5, bytes, bytes.length);
    }

    public static byte int2ByteArray(int i) {
        return (byte) (i & 0xFF);
    }

}
