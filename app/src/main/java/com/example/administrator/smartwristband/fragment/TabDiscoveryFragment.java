package com.example.administrator.smartwristband.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.discovery.AlarmActivity;
import com.example.administrator.smartwristband.activity.discovery.AntiLostActivity;
import com.example.administrator.smartwristband.activity.discovery.CallRemindActivity;
import com.example.administrator.smartwristband.activity.discovery.HRSActivity;
import com.example.administrator.smartwristband.activity.discovery.RSCActivity;
import com.example.administrator.smartwristband.bean.BLEFuctions;
import com.example.administrator.smartwristband.service.UartService;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TabDiscoveryFragment extends Fragment {

    private static TabDiscoveryFragment instance;
    private View view;
    private List<BLEFuctions> fuctionsList = new ArrayList<BLEFuctions>();
    private Activity mContext;
    private UartService mUartService = null;
    private static final String TAG = "TabDiscoveryFragment";
    private String address = null;
    private MyRecyclerViewAdapter mAdapter;

    //图标
    private int[] icno = {R.drawable.steps, R.drawable.heart, R.drawable.phone, R.drawable.lose, R.drawable.alarm};
    //图标下的文字
    private String[] name = {"步数", "心跳", "来电提醒", "防丢提醒", "闹钟提醒"};

    public TabDiscoveryFragment() {
        // Required empty public constructor
    }


    public static TabDiscoveryFragment newInstance() {
        if (instance == null) {
            instance = new TabDiscoveryFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        //开启服务
        Intent intent = new Intent(mContext, UartService.class);
        //绑定服务
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        //注册广播接受者
        mContext.registerReceiver(UartStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_tab_discovery, container, false);
        initView();
        return view;
    }

    //监视服务的状态
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获得中间人对象的方法
            mUartService = ((UartService.LocalBinder) service).getService();
            System.out.println("设备页面--连接服务成功！");
            if (!mUartService.initalize()) {
                Log.d(TAG, "unable to initalize Bluetooh");
                getActivity().finish();
            }
        }

        //当连接服务成功后
        @Override
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
        return intentFilter;
    }

    //注册广播接受者，监听服务发送的广播
    public final BroadcastReceiver UartStatusChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();//接收广播类型
            Log.d(TAG, "在TabDiscoveryFragment中接收到广播：" + action);
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                address = mUartService.getDeviceAddress();
            } else if (UartService.ACTION_GATT_DISCONNECTED.equals(action)) {
                address = null;
            }
        }
    };

    //初始化视图
    private void initView() {

        //初始化数据
        initFuctions();
        //通过findViewById拿到RecyclerView实例
        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView);
        //设置RecyclerView管理器
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mRecyclerView.addItemDecoration(new DividerGridViewItemDecoration(mContext));
        //初始化适配器
        mAdapter = new MyRecyclerViewAdapter(fuctionsList);
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //设置适配器
        mRecyclerView.setAdapter(mAdapter);

    }

    //初始化
    private void initFuctions() {
        BLEFuctions func;
        for (int i = 0; i < icno.length; i++) {
            func = new BLEFuctions(getRandomLengthName(name[i]), icno[i]);
            fuctionsList.add(func);
        }
    }

    private String getRandomLengthName(String name) {
        Random random = new Random();
        int i = random.nextInt(1) + 1;
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < i; j++) {
            sb.append(name);
        }
        return sb.toString();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUartService != null) {
            mContext.unbindService(mServiceConnection);
            mUartService = null;
        }
        try {
            mContext.unregisterReceiver(UartStatusChangeReceiver);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    //RecyclerView的适配器
    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
        private List<BLEFuctions> mList;

        public MyRecyclerViewAdapter(List<BLEFuctions> list) {
            mList = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(mContext, R.layout.discovery_gridview_item, null);
            final ViewHolder viewHolder = new ViewHolder(view);

            viewHolder.functionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = viewHolder.getAdapterPosition();
                    System.out.println("设备的地址是：" + address);
                    if (address != null) {
                        String text = fuctionsList.get(position).getName();
                        switch (text) {
                            case "步数":
                                Intent intent = new Intent(getActivity(), RSCActivity.class);
                                startActivity(intent);
                                break;
                            case "心跳":
                                Intent intents = new Intent(getActivity(), HRSActivity.class);
                                startActivity(intents);
                                break;
                            case "来电提醒":
                                Intent intentss = new Intent(getActivity(), CallRemindActivity.class);
                                startActivity(intentss);
                                break;
                            case "防丢提醒":
                                Intent intentLost = new Intent(getActivity(), AntiLostActivity.class);
                                startActivity(intentLost);
                                break;
                            case "闹钟提醒":
                                Intent intentAlarm = new Intent(getActivity(), AlarmActivity.class);
                                startActivity(intentAlarm);
                                break;

                        }
                    } else {
                        Toast.makeText(mContext, "请先连接设备", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BLEFuctions fuctions = mList.get(position);
            holder.imageView.setBackgroundResource(fuctions.getImageId());
            holder.mText.setText(fuctions.getName());
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mText;
            ImageView imageView;
            View functionView;

            ViewHolder(View itemView) {
                super(itemView);
                functionView = itemView;
                mText = itemView.findViewById(R.id.text);
                imageView = itemView.findViewById(R.id.img);
            }

        }
    }


    //绘制分割线
    //https://blog.csdn.net/tuike/article/details/79064750
    public class DividerGridViewItemDecoration extends RecyclerView.ItemDecoration {

        private Drawable mDivider;
        private int[] attrs = new int[]{
                android.R.attr.listDivider};

        public DividerGridViewItemDecoration(Context context) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs);
            mDivider = typedArray.getDrawable(0);
            typedArray.recycle();
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            c.drawColor(Color.parseColor("#00caff")); //绘制背景色
            drawVertical(c, parent);
            drawHorizontal(c, parent);
        }

        private void drawVertical(Canvas c, RecyclerView parent) {
            //绘制垂直间隔线（垂直的矩形）
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int left = child.getRight() + params.rightMargin;
                int right = left + mDivider.getIntrinsicWidth();
                int top = child.getTop() - params.topMargin;
                int bottom = child.getBottom() + params.bottomMargin;

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private void drawHorizontal(Canvas c, RecyclerView parent) {
            //绘制水平分割线
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int left = child.getLeft() - params.leftMargin;
                int right = child.getRight() + params.rightMargin + mDivider.getIntrinsicWidth();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // 四个方向的偏移值
            int right = mDivider.getIntrinsicWidth();
            int bottom = mDivider.getIntrinsicHeight();

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            int itemPosition = params.getViewAdapterPosition();
            if (isLastColum(itemPosition, parent)) {
                right = 0;
            }

            if (isLastRow(itemPosition, parent)) {
                bottom = 0;
            }
            outRect.set(0, 0, right, bottom);
        }

        /**
         * 是否最后一行
         */
        private boolean isLastRow(int itemPosition, RecyclerView parent) {
            int spanCount = getSpanCount(parent);
            if (spanCount != -1) {
                int childCount = parent.getAdapter().getItemCount();
                int lastRowCount = childCount % spanCount;
                //最后一行的数量小于spanCount
                if (lastRowCount == 0 || lastRowCount < spanCount) {
                    return true;
                }
            }

            return false;
        }


        /**
         * 根据parent获取到列数
         */
        private int getSpanCount(RecyclerView parent) {
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                GridLayoutManager lm = (GridLayoutManager) layoutManager;
                int spanCount = lm.getSpanCount();
                return spanCount;
            }
            return -1;
        }

        /**
         * 判断是否是最后一列
         */
        private boolean isLastColum(int itemPosition, RecyclerView parent) {
            int spanCount = getSpanCount(parent);
            if (spanCount != -1) {
                if ((itemPosition + 1) % spanCount == 0) {
                    return true;
                }
            }
            return false;
        }
    }

}
