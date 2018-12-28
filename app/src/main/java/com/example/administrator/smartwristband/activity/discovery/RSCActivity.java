package com.example.administrator.smartwristband.activity.discovery;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;
import com.example.administrator.smartwristband.ble.BaseBleMessage;
import com.example.administrator.smartwristband.ble.RSCGetData;
import com.example.administrator.smartwristband.service.UartService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RSCActivity extends BaseActivity {

    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private TextView mCadenceView;
    private TextView mCaloryUnitView;
    private TextView mCaloryView;
    private TextView mDistanceUnitView;
    private TextView mDistanceView;
    private Button mGetRSCButton;
    private Button mOpenRSCButton;
    private TextView mStridesCountView;
    private boolean isOpenRSC = false;
    private boolean isGetData = false;
    private TextView mActivityView;
    private RSCGetData rscgetdata;
    private final static String TAG = RSCActivity.class.getSimpleName();
    private UartService mUartServices;
    private Handler handler = new Handler();

    //注册服务
    private ServiceConnection mServiceConnections = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获得中间人对象的方法
            mUartServices = ((UartService.LocalBinder) service).getService();
            Log.d("RSCActivity", "计步页面---服务初始化成功");
            if (!mUartServices.initalize()) {
                Log.d("RSCActivity", "无法初始化");
                RSCActivity.this.finish();
                return;
            }
            if (mUartServices.connect(mUartServices.getDeviceAddress())) {
                Log.d(TAG, "计步页面---服务连接成功");
            } else {
                Log.d(TAG, "服务未连接");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUartServices = null;
            Log.d(TAG, "失败了");
        }
    };
    //注册广播接受者
    private final BroadcastReceiver uartStatusChangeReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("在RSCActivity接收到广播：+" + action);
            if (RSCGetData.ACTION_GET_RSC_DATA.equals(action)) {
                Log.d("RSCActivity", "解析计步数据");
                Log.d(TAG, "RSCActivity获取到的数据是" + intent.getIntExtra("length", 0));
                byte[] data = intent.getByteArrayExtra("rsc_data");
                int i = intent.getIntExtra("length", 0);
                System.out.println("在RSCActivity中获取到的值长度为：" + i);
                if (i != 0) {
                    rscgetdata.dealBleResponse(data, i);
                    onUpdateRSCinfo(rscgetdata.getSteps(), rscgetdata.getDistince(), rscgetdata.getCal(), rscgetdata.getSpeed());
                }
            }
        }

    };
    private Runnable runnable = runnable = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUartServices.setTx_data(rscgetdata.getData());
                }
            });
            handler.postDelayed(this, 2000);
        }
    };

    private void onUpdateRSCinfo(int steps, int distince, int cal, int speed) {
        mCaloryView.setText(String.valueOf(cal));
        mCadenceView.setText(String.format("%d", new Object[]{Integer.valueOf(speed)}));
        mDistanceView.setText(String.valueOf(distince));
        mStridesCountView.setText(String.valueOf(steps) + "步");

    }

    private void onClearRSCinfo() {
        mCaloryView.setText("");
        mCadenceView.setText("");
        mDistanceView.setText("");
        mStridesCountView.setText("");

    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrstep);
        ButterKnife.bind(this);
        initSystemBar(true);
        //初始化界面
        initView();
        //初始化服务
        service_init();

    }

    //初始化服务
    private void service_init() {
        //开启服务
        Intent intent = new Intent(RSCActivity.this, UartService.class);
        //绑定服务
        bindService(intent, mServiceConnections, Context.BIND_AUTO_CREATE);
        //注册广播接受者
        registerReceiver(uartStatusChangeReceivers, makeGattUpdateIntentFilters());
        System.out.println("RSCActivity中的广播注册成功");
    }

    private IntentFilter makeGattUpdateIntentFilters() {
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("com.example.wristband.rsc.ACTION_GET_RSC_DATA");
        return localIntentFilter;
    }

    //初始化界面
    private void initView() {
        // mCaloryUnitView = ((TextView) findViewById(R.id.calory_unit));
        //mDistanceUnitView = ((TextView) findViewById(R.id.distance_unit));
        //mActivityView = ((TextView) findViewById(R.id.activity));
        mCaloryView = ((TextView) findViewById(R.id.calory));
        mCadenceView = ((TextView) findViewById(R.id.cadence));
        mDistanceView = ((TextView) findViewById(R.id.distance));
        mStridesCountView = ((TextView) findViewById(R.id.strides));
        mOpenRSCButton = ((Button) findViewById(R.id.action_open));
        mGetRSCButton = ((Button) findViewById(R.id.action_getData));
        isOpenRSC = false;
        isGetData = true;
        tvMainTitle.setText("计步");
        tvSave.setVisibility(View.GONE);


        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RSCActivity.this.finish();
            }
        });

        mOpenRSCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpenRSC) {
                    isOpenRSC = false;
                    mOpenRSCButton.setText("关闭计步");
                    //打开计步--发送打开计步的指令
                    mUartServices.setTx_data(rscgetdata.onRSC());
                    handler.postDelayed(runnable, 1000);
                } else {
                    mOpenRSCButton.setText("打开计步");
                    isOpenRSC = true;
                    //关闭计步--发送关闭计步的指令
                    mUartServices.setTx_data(rscgetdata.offRSC());
                    handler.removeCallbacks(runnable);
                    onClearRSCinfo();
                }
            }
        });

        mGetRSCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("在RSCActivity页面发送数据" + BaseBleMessage.byteArrToString(rscgetdata.getData()));
                if (isGetData == true) {
                    isGetData = false;
                    mGetRSCButton.setText("停止获取数据");
                    handler.postDelayed(runnable, 1000);
                } else if (isGetData == false) {
                    isGetData = true;
                    mGetRSCButton.setText("获取数据");
                    handler.removeCallbacks(runnable);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        rscgetdata = new RSCGetData();
        handler.post(runnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(uartStatusChangeReceivers);
        this.unbindService(mServiceConnections);

    }
}
