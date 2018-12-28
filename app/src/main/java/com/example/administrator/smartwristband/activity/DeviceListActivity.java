package com.example.administrator.smartwristband.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.bean.BLEDevice;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceListActivity extends BaseActivity {

    private static final long SCAN_TIME = 3000;
    private static final int REQUEST_ENABLE_BL = 1;
    private static final int REQUEST_COARSE_LOCATION = 0;
    public static int rssi;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    @BindView(R.id.title_bar)
    RelativeLayout titleBar;
    @BindView(R.id.lv)
    ListView lv;
    @BindView(R.id.btn_cancle)
    Button btnCancle;
    private Handler handler;
    private boolean mScanning = true;
    private BluetoothManager mbluetoothManager;
    private BluetoothAdapter mbluetoothAdapter;
    private Button cancleButton;
    private BluetoothLeScanner mbluetoothLeScanner;
    private ListView listView;
    private List<BLEDevice> bleDeviceList = new ArrayList<BLEDevice>();
    private final static String TAG = DeviceListActivity.class.getSimpleName();


    final int msg3 = 103;
    final int msg4 = 104;
    private MyListADapter searchingRlAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initSystemBar(true);
        ButterKnife.bind(this);
        tvMainTitle.setText("已扫描设备");
        tvSave.setVisibility(View.GONE);
        //控件
        cancleButton = (Button) findViewById(R.id.btn_cancle);
        listView = (ListView) findViewById(R.id.lv);
        searchingRlAdapter = new MyListADapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = bleDeviceList.get(position).getName();
                String address = bleDeviceList.get(position).getMac();
                Intent intent = new Intent();
                intent.putExtra("name", name);
                intent.putExtra("address", address);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        //判断系统是否支持4.0
        checkBLEFeature();
        //获取蓝牙适配器
        mbluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mbluetoothAdapter = mbluetoothManager.getAdapter();
        mbluetoothLeScanner = mbluetoothAdapter.getBluetoothLeScanner();
        //检查蓝牙是否开启
        checkBluetoothOpen();
        bleDeviceList.clear();
    }

    public void click(View view) {
        //动态授权
        mayRequestLocation();
        //扫描设备
        scanDevice(mScanning);
    }


    //扫描设备
    public void scanDevice(boolean enable) {
        if (enable) {
            //每5秒自动停止扫描
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = true;
                    mbluetoothAdapter.stopLeScan(scanCallback);
                    //mbluetoothLeScanner.stopScan(scanCallback);
                    cancleButton.setText("开始扫描");
                }
            }, SCAN_TIME);

            mScanning = false;
            handle.sendEmptyMessage(msg3);
            mbluetoothAdapter.startLeScan(scanCallback);
            //mbluetoothLeScanner.startScan(scanCallback);
            cancleButton.setText("取消");
        } else {
            //停止扫描
            mScanning = true;
            mbluetoothAdapter.stopLeScan(scanCallback);
            //  mbluetoothLeScanner.stopScan(scanCallback);
            cancleButton.setText("开始扫描");
        }
    }

    //判断系统是否支持蓝牙4.0
    private void checkBLEFeature() {
        //判断系统是否支持蓝牙4.0
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(DeviceListActivity.this, "系统不支持", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    //判断蓝牙是否打开
    private void checkBluetoothOpen() {
        //判断是否支持蓝牙
        if (mbluetoothAdapter == null) {
            Toast.makeText(this, "不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            //蓝牙未打开，请求打开蓝牙
            if (!mbluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BL);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RESULT_OK) {
            return;
        } else if (requestCode == REQUEST_ENABLE_BL) {
            Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();

        }
    }

    //回调函数
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                public void run() {
                    //可以判断是否添加
                    if (!checkAddress(device.getAddress())) {
                        BLEDevice bleDevice = new BLEDevice(device.getName(), device.getAddress(), rssi);
                        bleDeviceList.add(bleDevice);
                        Log.d(TAG, "mLeScanCallback 搜索结果:设备名：" + device.getName() + "  MAC:" + device.getAddress());
                    }
                    //List加载适配器
                    if (searchingRlAdapter.isEmpty()) {
                        Log.d(TAG, "mLeDeviceListAdapter为空");
                    } else {
                        Log.d(TAG, "为mLeDeviceListAdapter添加新项");
                        listView.setAdapter(searchingRlAdapter);
                    }
                    handle.sendEmptyMessage(msg3);

                }
            });
        }
    };


    final Handler handle = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == msg3) {
                searchingRlAdapter.notifyDataSetChanged();
            }
        }
    };

    //动态申请权限
    private void mayRequestLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    Toast.makeText(this, "动态请求权限", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
                return;
            }
        }
    }

    //回调函数，获取用户是否授权
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //确保是我们的请求
        if (requestCode == REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限被授予", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //返回按钮
    @OnClick(R.id.tv_back)
    public void onViewClicked() {
        DeviceListActivity.this.finish();
    }

    //为ListView创建适配器
    public class MyListADapter extends BaseAdapter {
        @Override
        public int getCount() {
            return bleDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return bleDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = View.inflate(DeviceListActivity.this, R.layout.item_devicelist, null);
            } else {
                view = convertView;
            }
            TextView tvname = (TextView) view.findViewById(R.id.tv_name);
            TextView tvaddress = (TextView) view.findViewById(R.id.tv_mac);
            TextView tvdiss = (TextView) view.findViewById(R.id.tv_diss);
            tvname.setText((bleDeviceList.get(position).getName()) == null ? "Smart cup" : bleDeviceList.get(position).getName());
            tvaddress.setText(bleDeviceList.get(position).getMac());
            tvdiss.setText("信号：" + bleDeviceList.get(position).getRssi());
            return view;
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }
    }

    /**
     * 检查是否有重复设备绑定
     */
    private boolean checkAddress(String address) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            if (address.equals(bleDeviceList.get(i).getMac())) {
                return true;
            }
        }
        return false;
    }

    //    //定义回调函数
//    public android.bluetooth.le.ScanCallback scanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//            bleDeviceList.clear();
//            //获取设备
//            BluetoothDevice device = result.getDevice();
//            if (device != null) {
//                BLEDevice bleDevice = new BLEDevice(device.getName(), device.getAddress());
//                if (!bleDeviceList.contains(bleDevice)) {
//                    bleDeviceList.add(bleDevice);
//                }
//            }
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//            Log.e("1", "搜索失败");
//        }
//    };

}


