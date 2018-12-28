package com.example.administrator.smartwristband.activity.discovery;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;
import com.example.administrator.smartwristband.ble.BaseBleMessage;
import com.example.administrator.smartwristband.ble.BleRemindOnOff;
import com.example.administrator.smartwristband.service.UartService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AntiLostActivity extends BaseActivity {

    private static final String TAG = "AntiLostActivity";
    @BindView(R.id.imageLock)
    ImageView imageLock;
    @BindView(R.id.action_lost)
    Button actionLost;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private UartService uartCallService;
    private boolean openLostRemind = true;
    private int status;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uartCallService = ((UartService.LocalBinder) service).getService();
            Log.d(TAG, "防丢页面服务启动成功！");
            if (!uartCallService.initalize()) {
                Log.d(TAG, "无法初始化");
                AntiLostActivity.this.finish();
                return;
            }
            if (uartCallService.connect(uartCallService.getDeviceAddress())) {
                Log.d(TAG, "防丢页面---服务连接成功");
            } else {
                Log.d(TAG, "服务未连接");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            uartCallService = null;
            Log.d(TAG, "连接服务失败了");
        }
    };


    //注册广播接受者
    private final BroadcastReceiver uartStatusChangeReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("在AntiLostActivity接收到广播：+" + action);
            if (uartCallService.ACTION_GET_LOSTSTATUS.equals(action)) {
                int stas = UartService.byteToInt(intent.getByteExtra("rsc_data", (byte) 0x00));
                status = stas;
                Log.d(TAG, "AntiLostActivity获取到的数据是" + UartService.byteToInt(intent.getByteExtra("rsc_data", (byte) 0x00)));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initView();
                    }
                });
            }
        }

    };

    private IntentFilter makeGattUpdateIntentFilters() {
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("com.example.wristband.rsc.ACTION_LOSTSTATUS");
        return localIntentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anti_lost);
        initSystemBar(true);
        ButterKnife.bind(this);
        imageLock.setBackgroundResource(R.drawable.proximity_lock_closed);
        //启动服务
        bindService(new Intent(this, UartService.class), mServiceConnection, 0);
        //注册广播接受者
        registerReceiver(uartStatusChangeReceivers, makeGattUpdateIntentFilters());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                readStatus();
            }
        }, 500);
        tvMainTitle.setText("防丢提醒");
        tvSave.setVisibility(View.GONE);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AntiLostActivity.this.finish();
            }
        });

    }

    private void readStatus() {
        BleRemindOnOff bleRemindOnOff = new BleRemindOnOff();
        uartCallService.setTx_data(bleRemindOnOff.readRemindStatus(BleRemindOnOff.REMIND_TYPE_LOST));
    }

    public void onFindMeClicked(View view) {
        if (openLostRemind == false) {
            openLostRemind = true;
            actionLost.setText("关闭防丢");
            imageLock.setBackgroundResource(R.drawable.proximity_lock_closed);
            BleRemindOnOff bleRemindOnOff = new BleRemindOnOff();
            uartCallService.setTx_data(bleRemindOnOff.switchRemind(BleRemindOnOff.REMIND_TYPE_LOST, false));
            Log.d(TAG, "防丢页面向手环发送的指令是：" + bleRemindOnOff.switchRemind(BleRemindOnOff.REMIND_TYPE_LOST, false));

        } else if (openLostRemind == true) {
            openLostRemind = false;
            actionLost.setText("打开防丢");
            imageLock.setBackgroundResource(R.drawable.proximity_lock_open);
            BleRemindOnOff bleRemindOnOff = new BleRemindOnOff();
            uartCallService.setTx_data(bleRemindOnOff.switchRemind(BleRemindOnOff.REMIND_TYPE_LOST, true));
            Log.d(TAG, "防丢页面向手环发送的指令是：" + BaseBleMessage.byteArrToString(bleRemindOnOff.switchRemind(BleRemindOnOff.REMIND_TYPE_LOST, true)));
        }

    }

    public void initView() {
        if (status == 0) {
            openLostRemind = true;
            actionLost.setText("已关闭防丢");
            imageLock.setBackgroundResource(R.drawable.proximity_lock_closed);
        } else if (status == 1) {
            openLostRemind = false;
            actionLost.setText("已打开防丢");
            imageLock.setBackgroundResource(R.drawable.proximity_lock_open);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        unregisterReceiver(uartStatusChangeReceivers);

    }
}
