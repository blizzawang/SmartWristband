package com.example.administrator.smartwristband.activity.me;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;
import com.example.administrator.smartwristband.activity.MainActivity;
import com.example.administrator.smartwristband.bean.UserBean;
import com.example.administrator.smartwristband.sqlite.DBUtils;
import com.example.administrator.smartwristband.utils.MD5Utils;
import com.example.administrator.smartwristband.utils.SpInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private String userName, psw;
    private static final int REQUEST_REGISTER = 1;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.et_user_name)
    EditText etUserName;
    @BindView(R.id.et_psw)
    EditText etPsw;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.tv_register)
    TextView tvRegister;
    @BindView(R.id.tv_find_psw)
    TextView tvFindPsw;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initSystemBar(true);
        ButterKnife.bind(this);
        tvMainTitle.setText("登录");
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.finish();
            }
        });

        //跳转到注册页面
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_REGISTER);
            }
        });

        //跳转到找回密码页面
        tvFindPsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到找到密码界面
                Intent intent = new Intent(getApplicationContext(), FindPwdActivity.class);
                startActivity(intent);
            }
        });
    }

    //点击登录
    @OnClick(R.id.btn_login)
    public void onViewClicked() {
        userName = etUserName.getText().toString().trim();
        psw = etPsw.getText().toString();
        String md5Pwd = MD5Utils.md5Utils(psw);
        // 从数据库中获取是否存在正确的用户名
        UserBean bean = DBUtils.getInstance(getApplicationContext()).getUserInfo(userName);

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(md5Pwd)) {
            Toast.makeText(getApplicationContext(), "用户名或密码不能为空的", Toast.LENGTH_LONG).show();
            return;
        } else if (bean == null) {
            Toast.makeText(getApplicationContext(), "用户名不存在", Toast.LENGTH_LONG).show();
            return;
        } else if (!md5Pwd.equals(bean.getPwd())) {
            Toast.makeText(getApplicationContext(), "密码不正确", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_LONG).show();
            SpInfo.SaveInfo(getApplicationContext(), userName, psw, true);
            // 把登录信息传递到MainActivity中
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("isLogin", true);
            startActivity(intent);
            LoginActivity.this.finish();
            return;
        }

    }

}
