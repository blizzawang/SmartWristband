package com.example.administrator.smartwristband.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.me.LoginActivity;
import com.example.administrator.smartwristband.activity.me.UserInfoActivity;
import com.example.administrator.smartwristband.sqlite.DBUtils;
import com.example.administrator.smartwristband.utils.SpInfo;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TabMeFragment extends Fragment {

    public static final int REQUEST_USER_INFOR = 0;
    public static final int REQUEST_HEAD_LOGIN = 1;
    private static final int REQUEST_HISTORY_LOGIN = 2;
    private static final int REQUEST_SETTING_LOGIN = 3;
    private static TabMeFragment instance;
    @BindView(R.id.iv_head_icons)
    ImageView ivHeadIcons;
    @BindView(R.id.tv_user_name)
    TextView tvUserName;
    @BindView(R.id.ll_head)
    LinearLayout llHead;
    @BindView(R.id.rl_course_history)
    RelativeLayout rlCourseHistory;
    @BindView(R.id.rl_setting)
    RelativeLayout rlSetting;
    @BindView(R.id.rl_help)
    RelativeLayout rlHelp;
    @BindView(R.id.rl_loginout)
    RelativeLayout rlLoginout;
    private View view;
    private Activity mContext;
    private Bitmap bm;

    public static TabMeFragment newInstance() {
        if (instance == null) {
            instance = new TabMeFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //问题：onCreateView每次都调用导致的，这样fragment每次都会设置新的view，而调试发现，之前的view并没有被回收，这就导致了，新的view覆盖了之前设置的view，页面就会显示空白。
        //view（第一次加载时，可以将view保存下 来，再次加载时，判断保存下来的view是否为null，如果保存的view为null，返回新的view ，否则，先将 保存的view从父view中移除，然后将该view返回出去
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            return view;
        }
        view = inflater.inflate(R.layout.fragment_tab_me, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }


    @Override
    public void onResume() {
        initView();
        super.onResume();
    }

    //初始化布局
    private void initView() {
        // 设置登录时 界面控件的状态
        setLoginParams(SpInfo.readLoginStatus(mContext));
        // 点击头部布局的点击事件
        llHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpInfo.readLoginStatus(mContext)) {
                    //已登录，跳转到用户信息页面
                    Intent intent = new Intent(mContext, UserInfoActivity.class);
                    mContext.startActivityForResult(intent, REQUEST_USER_INFOR);

                } else {
                    //没有登录，跳转到登录页面
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    mContext.startActivityForResult(intent, REQUEST_HEAD_LOGIN);
                }
            }
        });

        rlCourseHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpInfo.readLoginStatus(mContext)) {
                    // 跳转
                    Toast.makeText(mContext, "历史记录 test", Toast.LENGTH_SHORT).show();
                } else {
                    showMessageFromDialog();
                }
            }

        });
        rlSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpInfo.readLoginStatus(mContext)) {
                    // 跳转到设置界面
                    //Intent intent = new Intent(mContext, SettingActitivy.class);
                    //mContext.startActivity(intent);
                    Toast.makeText(mContext, "设置界面 test", Toast.LENGTH_SHORT).show();

                } else {
                    showMessageFromDialog();
                }

            }
        });
        rlHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpInfo.readLoginStatus(mContext)) {
                    //跳转到帮助Activity
                    Toast.makeText(mContext, "帮助和反馈界面 test", Toast.LENGTH_SHORT).show();
                } else {
                    showMessageFromDialog();
                }

            }
        });

        rlLoginout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpInfo.readLoginStatus(mContext)) {
                    AlertDialog.Builder exitdialog = new AlertDialog.Builder(mContext);
                    exitdialog.setMessage("确定要退出吗？");
                    exitdialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            SpInfo.SaveInfo(mContext, "", "", false);
                            setLoginParams(false);
                        }
                    });
                    exitdialog.setNegativeButton("取消", null);
                    AlertDialog alertDialog = exitdialog.create();
                    alertDialog.show();
                    //需要在调用AlertDialog的show()方法后进行
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    Toast.makeText(mContext, "还没有登录哦", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    // 判断登录状态，如果登录显示用户名和默认的头像
    private void setLoginParams(boolean login) {
        // TODO Auto-generated method stub
        if (login) {
            String name = SpInfo.getInfo(mContext).get("username");
            String imagePath = DBUtils.getInstance(mContext).getUserInfo(name).getImages();
            String nickName = DBUtils.getInstance(mContext).getUserInfo(name).getNickName();
            bm = BitmapFactory.decodeFile(imagePath);
            tvUserName.setText(nickName);
            ivHeadIcons.setImageBitmap(bm);

        } else {
            tvUserName.setText("点击登录");
            ivHeadIcons.setImageResource(R.drawable.defaultuser_icon);
        }
    }

    private void showMessageFromDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setMessage("你还没有登录哦，请先登录");
        dialog.setPositiveButton("去登录", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Intent data = new Intent(mContext, LoginActivity.class);
                mContext.startActivityForResult(data, REQUEST_SETTING_LOGIN);

            }
        });
        dialog.setNegativeButton("残忍拒绝", null);

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
        //需要在调用AlertDialog的show()方法后进行
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


}
