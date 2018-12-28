package com.example.administrator.smartwristband.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.UFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.Log;

import com.example.administrator.smartwristband.activity.DeviceListActivity;
import com.example.administrator.smartwristband.activity.MainActivity;
import com.example.administrator.smartwristband.activity.discovery.RSCActivity;
import com.example.administrator.smartwristband.ble.BaseBleMessage;
import com.example.administrator.smartwristband.ble.BatteryMonitor;
import com.example.administrator.smartwristband.ble.RSCGetData;

import java.io.Closeable;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class UartService extends Service {


    private BluetoothManager mbluetoothManager;
    private BluetoothAdapter mbluetoothAdapter;
    private int mConnectionState = STATE_DISCONNECTED;
    public BluetoothGatt mbluetoothGatt;
    private String intentAction;
    private boolean isMonitor = false;
    private Handler mHandler = new Handler();
    private String mBluetoothDeviceAddress;//蓝牙设备地址
    public String mDeviceName;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public static final String ACTION_GET_LOSTSTATUS = "com.example.wristband.rsc.ACTION_LOSTSTATUS";
    public final static String ACTION_GET_RSC_DATA = "com.example.wristband.rsc.ACTION_GET_RSC_DATA";
    public final static String ACTION_GET_ALARM_DATA = "cn.example.wristband.rsc.ACTION_GET_ALARM_DATA";
    public final static String ACTION_GATT_CONNECTED = "com.charon.www.NewBluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.charon.www.NewBluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.charon.www.NewBluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.charon.www.NewBluetooth.ACTION_DATA_AVAILABLE";
    public final static String DEVICE_DOES_NOT_SUPPORT = "com.charon.www.NewBluetooth.DEVICE_DOES_NOT_SUPPORT";

    private final static String TAG = UartService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();

    //设备的UUID--建立连接
    public final static UUID RX_SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    //特征值1：Notify--连接建立成功
    public final static UUID RX_CHAR_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    //特征值2：表示可以向设备写入数据
    private final static UUID WX_CHAR_UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    //
    public final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
    private Runnable runnable;
    private Handler mHandlers = new Handler();
    private Runnable runnable1;

    public UartService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        ////把我们定义的中间人对象返回
        return mBinder;
    }

    //1定义一个中间人对象
    public class LocalBinder extends Binder {
        public UartService getService() {
            return UartService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void close() {
        if (mbluetoothGatt == null) {
            return;
        }
        mbluetoothGatt.close();
        mbluetoothGatt = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //初始化蓝牙适配器
    public boolean initalize() {
        if (mbluetoothManager == null) {
            //获取蓝牙适配器
            mbluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }
        mbluetoothAdapter = mbluetoothManager.getAdapter();
        if (mbluetoothAdapter == null) {
            Log.d("123", "不能获取BluetoothAdapter.");
            return false;
        }
        return true;
    }

    //连接BLE设备
    public boolean connect(final String address) {
        if (mbluetoothAdapter == null) {
            Log.w(TAG, "未初始化");
            return false;
        }
        //之前连接过设备
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mbluetoothGatt != null) {
            Log.d("123", "尝试使用现在的 mBluetoothGatt连接.");
            if (mbluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                Log.d("123", "连接成功！.");
                return true;
            } else {
                return false;
            }
        }
        //创建一个 BluetoothDevice 类实例(代表远程蓝牙实例)
        //device代表一个远程蓝牙设备。这个类可以让你连接所代表的蓝牙设备或者获取一些有关它的信息，例如它的名字，地址和绑定状态等等
        final BluetoothDevice device = mbluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.d(TAG, "设备没找到，不能连接");
            return false;
        }
        //connectGatt()通过这个方法获取到BluetoothGatt对象---是BLE设备和手机之间通信的管道对象
        //第二个参数表示是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
        //第三个参数是连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等。
        mbluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    //回调函数
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        //当调用蓝牙的连接方法之后，蓝牙会异步执行蓝牙连接的操作，如果连接成功会回调这个方法
        //第一个就蓝牙设备的 Gatt 服务连接类,
        //第二个参数代表是否成功执行了连接操作，如果为 BluetoothGatt.GATT_SUCCESS 表示成功执行连接操作
        //第三个参数才有效，否则说明这次连接尝试不成功
        //第三个参数代表当前设备的连接状态，如果 newState == BluetoothProfile.STATE_CONNECTED 说明设备已经连接.
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                //发送广播
                broadcastUpdate(intentAction);
                System.out.println("我连接上了：" + intentAction + "现在的状态是：" + newState);
                //搜索连接BLE设备所支持的服务，查找可读写的服务
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //表示未连接
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_CONNECTED;
                //发送广播
                broadcastUpdate(intentAction);
                System.out.println("失去连接了发送广播：" + intentAction + "现在的状态是：" + newState);
                gatt.close();
            }

        }

        //在onConnectionStateChang方法被成功回调且表示成功连接之后才会调用 onServicesDiscovered 这一个方法
        //当这个方法被调用之后，系统会异步执行发现服务的过程，直到onServicesDiscovered 被系统回调之后，手机设备和蓝牙设备才算是真正建立了可通信的连接。
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //如果连接成功
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //获取所有服务---以及服务的特征的 所有的UUID
                List<BluetoothGattService> services = gatt.getServices();
                for (int i = 0; i < services.size(); i++) {
                    Log.e(TAG, "1:BluetoothGattService UUID=:" + services.get(i).getUuid());
                    //获取每个服务的所有的特征值
                    BluetoothGattService bluetoothGattService = services.get(i);
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        Log.e("a", "2:BluetoothGattCharacteristic UUID=:" + characteristics.get(j).getUuid());
                    }
                }
                enableTXNotification();
                startMonitorBattery();
                intentAction = ACTION_GATT_SERVICES_DISCOVERED;
                broadcastUpdate(intentAction);
            } else {
                Log.e(TAG, "onservicesdiscovered收到失败的状态: " + status);
            }
        }

        //特征(Characteristic)启用通知,当远程蓝牙设备特性发送变化，回调函数onCharacteristicChanged( ))被触发。
        //简单说就是---这个回调函数会接收BLE设备返回的数据
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG, "BLE向手机返回的数据:" + BaseBleMessage.byteArrToString(characteristic.getValue()));
            UUID uuid = characteristic.getUuid();
            //获取返回的值
            byte[] value = characteristic.getValue();
            if (uuid.equals(RX_CHAR_UUID)) {
                Log.e(TAG, "获取到数据-特征的UUID:" + characteristic.getUuid().toString());
                broadcastUpdate(ACTION_DATA_AVAILABLE);
                if (value.length > 1) {
                    Log.e(TAG, "第1个byte:" + byteToInt(value[1]));
                }
                //对返回的值进行处理
                cmd_Handle(value);
            }
        }

        //当手机向BLE设备写入到数据中的时候，会触发这个方法
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            byte[] data = characteristic.getValue();
            Log.e(TAG, "写入的数据-的特征UUID: " + characteristic.getUuid().toString());
            System.out.println("手机向BLE写的数据:" + BaseBleMessage.byteArrToString(data));
        }
    };

    //断开设备
    public boolean disconnent() {
        if (mbluetoothAdapter == null) {
            Log.w("123", "BluetoothAdapter not initialized");
        }
        mbluetoothGatt.disconnect();
        return true;
    }

    //发送广播
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);//发送广播

    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mbluetoothGatt == null)
            return null;
        return mbluetoothGatt.getServices();
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mbluetoothAdapter == null || mbluetoothGatt == null) {
            Log.w("123", "BluetoothAdapter not initialized");
            return;
        } else mbluetoothGatt.writeCharacteristic(characteristic);

    }

    //开启通知--接收BLE设备的返回的通知
    public void enableTXNotification() {
        //获取我们使用的服务-通过UUID
        BluetoothGattService mbluetoothGattService = mbluetoothGatt.getService(RX_SERVICE_UUID);
        if (mbluetoothGattService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT);
            return;
        }
        //获取--通知的特征对象
        BluetoothGattCharacteristic characteristic = mbluetoothGattService.getCharacteristic(RX_CHAR_UUID);
        if (characteristic == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT);
            return;
        }
        mbluetoothGatt.setCharacteristicNotification(characteristic, true);
        Log.d(TAG, "TxChar:" + characteristic.getValue());
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.mbluetoothGatt.writeDescriptor(descriptor);
        Log.d(TAG, "descriptor:" + (descriptor.getValue()));
    }

    //获取BLE手环电量--每个5秒获取
    private void startMonitorBattery() {
        isMonitor = true;
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isMonitor) {
                    setTx_data(new BatteryMonitor().getBattery());
                    mHandler.postDelayed(this, 30000);
                }
            }
        };
        mHandler.postDelayed(runnable, 500);
    }

    //停止取电量
    private void stopMonitorBattery() {
        mHandler.removeCallbacks(runnable);
    }


    private int tx_data_len = 0;
    int j = 0;
    int len;

    //发送指令的方法
    public void setTx_data(final byte[] tx_data) {
        tx_data_len = tx_data.length;
        if (tx_data_len <= 20) {
            writeRXCharacteristic(tx_data);
        } else {
            runnable1 = new Runnable() {
                @Override
                public void run() {
                    if (tx_data_len > 20) {
                        len = 20;
                    } else {
                        len = tx_data_len;
                    }
                    byte[] send_buffer = new byte[len];
                    for (int i = 0; i < len; i++) {
                        send_buffer[i] = tx_data[j + i];
                    }
                    writeRXCharacteristic(send_buffer);
                    Log.d(TAG, "setTx_data: +这个方法发送的指令是：" + BaseBleMessage.byteArrToString(send_buffer));
                    tx_data_len = tx_data_len - len;
                    j = len;
                    if (tx_data_len > 0) {
                        mHandlers.postDelayed(this, 200);
                    } else if (tx_data_len == 0) {
                        len = 0;
                        j = 0;
                        mHandlers.removeCallbacks(this);
                    }
                }
            };
            mHandlers.postDelayed(runnable1, 200);
        }
    }


    // 手机写到到BLE设备数据的方法
    public void writeRXCharacteristic(byte[] paramArrayOfByte) {
        if (this.mbluetoothGatt == null) {
            return;
        }
        BluetoothGattService localBluetoothGattService = mbluetoothGatt.getService(RX_SERVICE_UUID);
        if (localBluetoothGattService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate("com.hch.ble.DEVICE_DOES_NOT_SUPPORT");
            return;
        }
        //获取-----写入的特征对象
        BluetoothGattCharacteristic localBluetoothGattCharacteristic = localBluetoothGattService.getCharacteristic(WX_CHAR_UUID);
        if (localBluetoothGattCharacteristic == null) {
            showMessage("Rx charateristic not found!");
            broadcastUpdate("com.hch.ble.DEVICE_DOES_NOT_SUPPORT");
            return;
        }
        //设置写入手环的内容
        localBluetoothGattCharacteristic.setValue(paramArrayOfByte);

        boolean bool = mbluetoothGatt.writeCharacteristic(localBluetoothGattCharacteristic);
        localBluetoothGattService.getCharacteristic(WX_CHAR_UUID);
        Log.d(TAG, "writeTXchar - status=" + bool);
    }

    //打印错误日志文件
    private void showMessage(String paramString) {
        Log.e(TAG, paramString);
    }


    private int bufferFront = 0;
    private int bufferLen = 0;
    private int bufferRear = 0;
    private byte[] buffer;
    StringBuffer sb;

    //把从BLE设备返回的数据进行合并
    public void doParse(byte[] byteData) {
        //先把十六进制数字转化成字符串
        Log.d(TAG, "对接收的数据处理：" + BaseBleMessage.byteArrToString(byteData));
        String content = BaseBleMessage.byteArrToString(byteData);
        sb.append(content);
        Log.d(TAG, "doParse: " + sb.toString());
        if (byteData.length < 20) {
            sb = null;
        }

    }

    //对返回的数据进行类型判断
    private void cmd_Handle(byte[] value) {
        int length = value.length;
        if (length == 1) {
            return;
        } else {
            if (byteToInt(value[1]) == 131 && value[0] == 0x68 && length == 7)//表示BLE回复的查询电量的命令
            {
                Log.d(TAG, "接收的数据是电量信息");
                //发送广播
                Intent batteryIntent = new Intent(ACTION_GET_ALARM_DATA);
                batteryIntent.putExtra("batterydata", byteToInt(value[4]));
                sendBroadcast(batteryIntent);
            } else if (length == 20 && byteToInt(value[1]) == 134 && value[0] == 0x68)//表示回复的是步数，心率
            {
                Log.d(TAG, "进入测步和心率实验---");
                byte[] stepbyte = new byte[15];
                for (int i = 0; i < stepbyte.length; i++) {
                    stepbyte[i] = value[i + 4];
                }
                System.out.println("合成的数据是" + BaseBleMessage.byteArrToString(stepbyte));
                System.out.println("合成的数据是长度是：" + stepbyte.length);
                //发送广播
                Intent stepIntent = new Intent();
                stepIntent.putExtra("rsc_data", stepbyte);
                stepIntent.putExtra("length", stepbyte.length);
                stepIntent.setAction(ACTION_GET_RSC_DATA);
                sendBroadcast(stepIntent);
                Log.d(TAG, "cmd_Handle发送了广播:" + ACTION_GET_RSC_DATA);
            } else if (byteToInt(value[1]) == 129) {
                Log.d(TAG, "来电提醒已经执行成功，请检查手环++++");
            } else if (byteToInt(value[1]) == 133) {
                byte statusByte = value[6];
                //发送广播
                Intent lostIntent = new Intent();
                lostIntent.putExtra("rsc_data", statusByte);
                lostIntent.setAction(ACTION_GET_LOSTSTATUS);
                sendBroadcast(lostIntent);
                Log.d(TAG, "防盗页面开启成功！---");
                Log.d(TAG, "防盗页面返回的指令为" + BaseBleMessage.byteArrToString(value));
            }

        }
    }


    //把字节转换成Int
    public static int byteToInt(byte paramByte) {
        return paramByte & 0xFF;
    }

    //获取BLE设备的地址
    public String getDeviceAddress() {
        return mBluetoothDeviceAddress;
    }

    public String getDeviceName() {
        return mDeviceName;
    }


}

