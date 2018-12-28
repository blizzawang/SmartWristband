package com.example.administrator.smartwristband.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.DeviceListActivity;
import com.example.administrator.smartwristband.ble.RSCGetData;
import com.example.administrator.smartwristband.service.UartService;

import static android.app.Activity.RESULT_OK;

public class TabHomeFragment extends Fragment implements View.OnClickListener {
    private static final int CHOOSE_DEVICE = 1;
    private Activity mContext;
    private UartService mUartService;

    private final static String TAG = TabHomeFragment.class.getSimpleName();
    private TextView mDeciceName;
    private TextView mDeciceStatus;
    private TextView mDecieElectric;
    private Button btnSearch;
    private Button btnConnent;
    private Button btnDisConnent;
    private String address;
    private Button btnDelete;
    private String name;
    private static TabHomeFragment instance = null;
    private View view;

    public TabHomeFragment() {

    }

    public static TabHomeFragment newInstance() {
        if (instance == null) {
            instance = new TabHomeFragment();
        }
        return instance;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (Activity) getActivity();
        //服务初始化和开启广播
        service_init();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            return view;
        }
        view = inflater.inflate(R.layout.fragment_tab_home, container, false);
        mDeciceName = (TextView) view.findViewById(R.id.tv_name);
        mDeciceStatus = (TextView) view.findViewById(R.id.tv_status);
        mDecieElectric = (TextView) view.findViewById(R.id.tv_electric);
        btnSearch = view.findViewById(R.id.btn_search);
        btnConnent = view.findViewById(R.id.btn_connent);
        btnDisConnent = view.findViewById(R.id.btn_disconnent);
        //btnDelete = view.findViewById(R.id.btn_delete);
        btnSearch.setOnClickListener(this);
        btnConnent.setOnClickListener(this);
        btnDisConnent.setOnClickListener(this);
        //btnDelete.setOnClickListener(this);
        mDeciceStatus.setText("状态");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //初始化服务
    private void service_init() {
        //开启服务
        Intent intent = new Intent(mContext, UartService.class);
        //绑定服务
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        //注册广播接受者
        mContext.registerReceiver(UartStatusChangeReceiver, makeGattUpdateIntentFilter());
        //注册本地广播接受者，收不到service发送的消息---弃用
        //LocalBroadcastManager.getInstance(mContext).registerReceiver(UartStatusChangeReceiver, makeGattUpdateIntentFilter());

    }

    //监视服务的状态
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        ////当连接服务成功后
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获得中间人对象的方法
            mUartService = ((UartService.LocalBinder) service).getService();
            System.out.println("设备页面--连接服务成功！");
            if (!mUartService.initalize()) {
                Log.d(TAG, "unable to initalize Bluetooh");
                getActivity().finish();
            }
        }

        //失去连接
        public void onServiceDisconnected(ComponentName name) {
            mUartService = null;
            Log.d(TAG, "onServiceDisconnected:设备页面失去连接了");
        }
    };

    //注册广播接收者参数
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT);
        intentFilter.addAction(UartService.ACTION_GET_ALARM_DATA);
        return intentFilter;
    }

    //注册广播接受者，监听服务发送的广播
    public final BroadcastReceiver UartStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();//接收广播类型
            System.out.println("在Tab中接收到广播：" + action);
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "UART_CONNECT_MSG");
                        mDeciceName.setText(name);
                        mDeciceStatus.setText("已连接");
                    }
                });
            } else if (action.equals(UartService.ACTION_GET_ALARM_DATA)) {
                final int batterydata = intent.getIntExtra("batterydata", 0);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "更新手环电量");
                        mDeciceStatus.setText("已连接");
                        mDecieElectric.setText(batterydata + "%");
                    }
                });
            } else if (UartService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "UART_DISCONNECT_MSG");
                mDeciceStatus.setText("未连接");
                mDecieElectric.setText("");
                if (mUartService != null) {
                    mUartService.close();
                }

            } else if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                System.out.println("读写数据");
            }
        }
    };


    //根据不同的点击事件触发不同的操作：
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_search:
                //点击选择设备
                Intent intent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(intent, CHOOSE_DEVICE);
                break;
            case R.id.btn_connent:
                //点击连接设备
                if (address != null) {
                    mUartService.connect(address);
                } else {
                    Toast.makeText(mContext, "请先选择设备", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_disconnent:
                //点击断开设备
                if (mDeciceStatus.getText() == "未连接") {
                    Toast.makeText(mContext, "已断开", Toast.LENGTH_SHORT).show();
                } else if (mDeciceStatus.getText() == "状态") {
                    Toast.makeText(mContext, "未连接", Toast.LENGTH_SHORT).show();

                } else {
                    mUartService.disconnent();
                }
                break;

            //case R.id.btn_delete:
               // break;
            default:
                break;

        }

    }

    //接收从蓝牙列表返回的数据
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_DEVICE && resultCode == RESULT_OK) {
            name = data.getStringExtra("name");
            mDeciceName.setText(data.getStringExtra("name"));
            address = data.getStringExtra("address");
        }

    }

    //关闭广播接收者 --停止服务
    public void onDestroy() {
        super.onDestroy();
        try {
            mContext.unregisterReceiver(UartStatusChangeReceiver);
            // LocalBroadcastManager.getInstance(mContext).unregisterReceiver(UartStatusChangeReceiver);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (mUartService != null) {
            mContext.unbindService(mServiceConnection);
            // mUartService.stopSelf();
            mUartService = null;
        }
    }


}
