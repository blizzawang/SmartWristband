package com.example.administrator.smartwristband.activity.discovery;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;
import com.example.administrator.smartwristband.ble.SetWristDateTime;
import com.example.administrator.smartwristband.service.UartService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlarmActivity extends BaseActivity {

    private static final String TAG = "AlarmActivity";
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private UartService uartAlarmService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uartAlarmService = ((UartService.LocalBinder) service).getService();
            Log.d(TAG, "提醒页面服务启动成功！");
            if (!uartAlarmService.initalize()) {
                Log.d(TAG, "无法初始化");
                AlarmActivity.this.finish();
                return;
            }
            if (uartAlarmService.connect(uartAlarmService.getDeviceAddress())) {
                Log.d(TAG, "提醒页面---服务连接成功");
            } else {
                Log.d(TAG, "服务未连接");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            uartAlarmService = null;
            Log.d(TAG, "连接服务失败了");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        initSystemBar(true);
        tvMainTitle.setText("闹铃提醒");
        tvSave.setVisibility(View.GONE);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmActivity.this.finish();
            }
        });


        bindService(new Intent(this, UartService.class), mServiceConnection, 0);
    }

    //同步时间
    public void onSyncTime(View view) {
        //发送同步时间的指令
        uartAlarmService.setTx_data(new SetWristDateTime().sendDatetowrister());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
