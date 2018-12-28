package com.example.administrator.smartwristband.ble;

public class BatteryMonitor extends BaseBleMessage {

    //查询手环电量命令
    public byte[] getBattery() {
        byte[] data = new byte[2];
        data[0] = 0x00;
        data[1] = 0x00;
        return setMessageByteData(0x03, data, 2);
    }

    private byte[] setMessageByteData(int mTheCmd, byte[] data, int length) {
        byte[] datas = new byte[6];
        datas[0] = 0x68;
        datas[1] = (byte) mTheCmd;
        for (int i = 0; i < length; i++) {
            datas[i + 2] = data[i];
        }
        datas[4] = 0x6B;
        datas[5] = 0x16;

        return datas;
    }


}
