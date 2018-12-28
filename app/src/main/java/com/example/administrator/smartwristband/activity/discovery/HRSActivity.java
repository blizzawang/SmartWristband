package com.example.administrator.smartwristband.activity.discovery;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;
import com.example.administrator.smartwristband.ble.BaseBleMessage;
import com.example.administrator.smartwristband.ble.HRSgetData;
import com.example.administrator.smartwristband.service.UartService;
import com.example.administrator.smartwristband.utils.ChartService;

import org.achartengine.GraphicalView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HRSActivity extends BaseActivity {


    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private GraphicalView mGraphView;
    private ChartService mLineGraph;
    private HRSgetData mHRSdata;
    private UartService uartHRService;
    private HandlerThread ht;
    private Handler handler;
    private boolean isStart = false;
    private boolean isGraphInProgress = false;
    private boolean isrunning = false;
    private int mHrmValue = 0;
    private int mCounter = 0;
    private byte[] data;
    private byte[] command;
    private Handler mHandler = new Handler();
    private final static String TAG = HRSActivity.class.getSimpleName();
    @BindView(R.id.text_hrs_value)
    TextView textHrsValue;
    @BindView(R.id.text_hrs_position)
    TextView textHrsPosition;
    @BindView(R.id.graph_hrs)
    FrameLayout graphHrs;
    @BindView(R.id.action_start)
    Button btn_Start;

    private final BroadcastReceiver mHrsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "在心率页面接收到广播：:" + intent.getAction());
            String action = intent.getAction();
            if (HRSgetData.ACTION_GET_HRS_DATA.equals(action)) {
                byte[] data = intent.getByteArrayExtra("rsc_data");
                int i = intent.getIntExtra("length", 0);
                Log.d(TAG, "在心跳页面接收到的数据长度是：" + i);
                Log.d(TAG, "在心跳页面接收到的数据是：" + BaseBleMessage.byteArrToString(data));
                mHRSdata.dealBleResponse(data, i);
                i = mHRSdata.getHRS();
                mHrmValue = i;
                Log.d(TAG, "接收到的心率是：" + String.valueOf(i));
                setHRSValueOnView(i);
            }
        }

    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uartHRService = ((UartService.LocalBinder) service).getService();
            Log.d(TAG, "心率页面服务启动成功！");
            if (!HRSActivity.this.uartHRService.initalize()) {
                Log.d(TAG, "无法初始化");
                HRSActivity.this.finish();
                return;
            }
            if (uartHRService.connect(uartHRService.getDeviceAddress())) {
                Log.d(TAG, "心率页面---服务连接成功");
            } else {
                Log.d(TAG, "服务未连接");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            uartHRService = null;
            Log.d(TAG, "连接服务失败了");
        }
    };

    //开启线程，根据获取的心里值，不断绘制图像
    private Runnable mRepeatTask = new Runnable() {
        public void run() {
            if (mHrmValue > 0) {
                updateGraph(mHrmValue);
            }
            if (isGraphInProgress) {
                mHandler.postDelayed(mRepeatTask, 1000);
            }
        }
    };

    private Runnable runnable = new Runnable() {
        public void run() {
            if (isrunning) {
                uartHRService.setTx_data(command);
                handler.postDelayed(runnable, 1000);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrs);
        initSystemBar(true);
        ButterKnife.bind(this);
        tvMainTitle.setText("心跳");
        tvSave.setVisibility(View.GONE);
        mLineGraph = ChartService.getGraphView(this);
        showGraph();
        mHRSdata = new HRSgetData();
        registerbroadcast();
        //创建HandlerThrea对象
        ht = new HandlerThread("handler thread");
        //开启
        ht.start();
        //创建Handler，使用mHandlerThread.getLooper()生成Looper
        handler = new Handler(ht.getLooper());
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HRSActivity.this.finish();
            }
        });
    }

    private void showGraph() {
        mGraphView = mLineGraph.getGraphicalView();
        //addView动态的添加控件到界面上
        graphHrs.addView(mGraphView);
    }

    public void onStartClicked(View view) {
        if (!isStart) {
            isStart = true;
            btn_Start.setText("停止");
            clearGraph();
            command = mHRSdata.onHRS();
            data = mHRSdata.getHRSData();
            Intent localIntent = new Intent();
            localIntent.putExtra("start", command);
            localIntent.putExtra("hrs", true);
            localIntent.setAction(HRSgetData.ACTION_OPEN_HRS);
            sendBroadcast(localIntent);
            isrunning = true;
            startShowGraph();
            handler.postDelayed(runnable, 1000);
            return;
        }
        isStart = false;
        btn_Start.setText("开始");
        stopShowGraph();
        this.isrunning = false;
        this.handler.removeCallbacks(this.runnable);
        this.mHandler.removeCallbacks(this.mRepeatTask);
        command = this.mHRSdata.offHRS();
        Intent localIntent = new Intent();
        localIntent.putExtra("stop", command);
        localIntent.putExtra("hrs", false);
        localIntent.setAction(HRSgetData.ACTION_CLOSE_HRS);
        sendBroadcast(localIntent);

    }

    private void stopShowGraph() {
        isGraphInProgress = false;
        mHandler.removeCallbacks(mRepeatTask);
        handler.removeCallbacks(runnable);
    }

    private void clearGraph() {
        mLineGraph.clearGraph();
        mGraphView.repaint();
        mCounter = 0;
        mHrmValue = 0;
    }

    void startShowGraph() {
        isGraphInProgress = true;
        mRepeatTask.run();
    }

    private void updateGraph(int mHrmValue) {
        mCounter += 1;
        mLineGraph.updateChart(this.mCounter, mHrmValue);
    }

    //开启服务和注册广播接收者
    private void registerbroadcast() {
        bindService(new Intent(this, UartService.class), this.mServiceConnection, 0);
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(HRSgetData.ACTION_GET_HRS_DATA);
        registerReceiver(mHrsReceiver, localIntentFilter);
    }


    //显示心率数值
    private void setHRSValueOnView(final int paramInt) {
        runOnUiThread(new Runnable() {
            public void run() {
                if ((paramInt >= 0) && (paramInt <= 65535)) {
                    textHrsValue.setText(Integer.toString(paramInt));
                    return;
                }
                textHrsValue.setText(R.string.not_available);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopShowGraph();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHrsReceiver);
        this.unbindService(mServiceConnection);
    }
}


