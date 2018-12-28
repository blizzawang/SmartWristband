package com.example.administrator.smartwristband.bean;

public class BLEDevice {
    public  BLEDevice(String name ,String mac,int rssi)
    {
        this.name=name;
        this.mac=mac;
        this.rssi=rssi;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    private String  name;
    private String mac;

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    private int rssi;

}
