package com.example.administrator.smartwristband.activity.discovery;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;
import com.example.administrator.smartwristband.bean.Contact;
import com.example.administrator.smartwristband.ble.BaseBleMessage;
import com.example.administrator.smartwristband.ble.CallReminderOperate;
import com.example.administrator.smartwristband.service.UartService;
import com.example.administrator.smartwristband.utils.QueryContactsUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CallRemindActivity extends BaseActivity {

    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static final int REQUEST_READ_CONTECT_STATE = 2;
    @BindView(R.id.imageLock)
    ImageView imageLock;
    @BindView(R.id.action_openMessage)
    Button actionOpenMessage;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private String TAG = CallRemindActivity.class.getSimpleName();
    private UartService uartCallService;
    private boolean openCallRemind = true;
    private boolean openMessageRemind = true;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uartCallService = ((UartService.LocalBinder) service).getService();
            Log.d(TAG, "电话页面服务启动成功！");
            if (!uartCallService.initalize()) {
                Log.d(TAG, "无法初始化");
                CallRemindActivity.this.finish();
                return;
            }
            if (uartCallService.connect(uartCallService.getDeviceAddress())) {
                Log.d(TAG, "电话提醒页面---服务连接成功");
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

    @BindView(R.id.action_open)
    Button actionOpen;
    private TelephonyManager telephony;
    private OnePhoneStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_remind);
        initSystemBar(true);
        ButterKnife.bind(this);
        tvMainTitle.setText("来电提醒");
        tvSave.setVisibility(View.GONE);
        imageLock.setBackgroundResource(R.drawable.proximity_lock_closed);
        //启动服务
        bindService(new Intent(this, UartService.class), mServiceConnection, 0);
        //动态申请权限
        mayRequestLocation();
        //获电话管理类--注意这里一定是Service.TELEPHONY_SERVICE
        telephony = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        listener = new OnePhoneStateListener();
        telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallRemindActivity.this.finish();
            }
        });
    }


    //来电监听
    class OnePhoneStateListener extends PhoneStateListener {

        private List<Contact> contactList;

        //电话状态监听，电话来了会回调onCallStateChanged()方法
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.d(TAG, "onCallStateChanged: 电话号码是:" + incomingNumber + "  状态是：" + state);
            if (state == TelephonyManager.CALL_STATE_RINGING)// 电话响铃
            {
                String name = null;
                //获取联系人列表
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactList = QueryContactsUtils.queryContacts(CallRemindActivity.this);
                    }
                });
                for (Contact item : contactList) {
                    //获取的电话可能为空
                    String phone = item.getPhone();
                    if (phone != null) {
                        Log.d(TAG, "输出联系人是：" + item.getName() + " 电话是：" + phone);
                        if (phone.replace(" ", "").contains(incomingNumber)) {
                            name = item.getName();
                            Log.d(TAG, "来电的联系人名字是：" + name);
                            break;
                        }
                    }
                }
                CallReminderOperate localCallReminderOperate1 = new CallReminderOperate();
                uartCallService.setTx_data(localCallReminderOperate1.sendReminder(incomingNumber, name));
                Log.d(TAG, "onCallStateChanged: 电话是" + incomingNumber + " 姓名是:" + name);
                Log.d(TAG, "CallRemindActivity 发送的指令是：" + BaseBleMessage.byteArrToString(localCallReminderOperate1.sendReminder(incomingNumber, name)));
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK)//接听或者挂断
            {
                CallReminderOperate localCallReminderOperate2 = new CallReminderOperate();
                uartCallService.setTx_data(localCallReminderOperate2.closeCallReminder());

            } else if (state == TelephonyManager.CALL_STATE_IDLE)// 空闲，就是无电话状态
            {

            }
        }
    }

    //点击事件
    @OnClick(R.id.action_open)
    public void onViewClicked() {
        if (openCallRemind == false) {
            openCallRemind = true;
            actionOpen.setText("打开来电提醒");
            imageLock.setBackgroundResource(R.drawable.proximity_lock_closed);
            CallReminderOperate localCallReminderOperate3 = new CallReminderOperate();
            uartCallService.setTx_data(localCallReminderOperate3.closeCallReminder());
            Log.d(TAG, " 向手环发送的电环的指令：" + BaseBleMessage.byteArrToString(localCallReminderOperate3.closeCallReminder()));

        } else if (openCallRemind == true) {
            openCallRemind = false;
            actionOpen.setText("关闭来电提醒");
            imageLock.setBackgroundResource(R.drawable.proximity_lock_open);
            CallReminderOperate localCallReminderOperate3 = new CallReminderOperate();
            uartCallService.setTx_data(localCallReminderOperate3.sendOpenreminder());
            Log.d(TAG, "向手环发送的指令是：" + BaseBleMessage.byteArrToString(localCallReminderOperate3.sendOpenreminder()));
        }
    }

    //打开短信提醒
    public void openMessage(View view) {

        if (openMessageRemind == false) {
            openMessageRemind = true;
            actionOpenMessage.setText("打开短信提醒");
            //imageLock.setBackgroundResource(R.drawable.proximity_lock_closed);
            CallReminderOperate localCallReminderOperate3 = new CallReminderOperate();
            uartCallService.setTx_data(localCallReminderOperate3.closeCallReminder());
            Log.d(TAG, " 向手环发送的电环的指令：" + BaseBleMessage.byteArrToString(localCallReminderOperate3.closeCallReminder()));

        } else if (openMessageRemind == true) {
            openMessageRemind = false;
            actionOpenMessage.setText("关闭短信提醒");
            //imageLock.setBackgroundResource(R.drawable.proximity_lock_open);
            CallReminderOperate localCallReminderOperate3 = new CallReminderOperate();
            uartCallService.setTx_data(localCallReminderOperate3.sendOpenMessagereminder());
            Log.d(TAG, "向手环发送的指令是：" + BaseBleMessage.byteArrToString(localCallReminderOperate3.sendOpenreminder()));
        }
    }


    private static String[] PERMISSIONS_PHONE_CONTACTS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};

    //动态申请权限
    private void mayRequestLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            int checkReadContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED || checkReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_PHONE_CONTACTS, REQUEST_READ_CONTECT_STATE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("imei", "权限被允许");
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.d("imei", "权限被拒绝");
                //这里表示申请权限后被用户拒绝了
            }
        } else if (requestCode == REQUEST_READ_CONTECT_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("imei", "读联系人权限被允许");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        telephony.listen(listener, 0);
        unbindService(mServiceConnection);
    }
}
