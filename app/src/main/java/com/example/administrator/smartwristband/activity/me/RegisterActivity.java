package com.example.administrator.smartwristband.activity.me;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;
import com.example.administrator.smartwristband.bean.UserBean;
import com.example.administrator.smartwristband.sqlite.DBUtils;
import com.example.administrator.smartwristband.utils.MD5Utils;
import com.example.administrator.smartwristband.utils.SpInfo;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private String userName, psw, pswAgain;
    @BindView(R.id.et_user_name)
    EditText etUserName;
    @BindView(R.id.et_psw)
    EditText etPsw;
    @BindView(R.id.et_psw_again)
    EditText etPswAgain;
    @BindView(R.id.btn_register)
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initSystemBar(true);
        ButterKnife.bind(this);
        tvMainTitle.setText("注册");
        tvSave.setVisibility(View.GONE);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
            }
        });
    }

    @OnClick(R.id.btn_register)
    public void onViewClicked() {
        // 获取输入在相应控件中的字符串
        getEditString();
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(getApplicationContext(), "请输入用户名", 0).show();
            return;
        } else if (TextUtils.isEmpty(psw)) {
            Toast.makeText(getApplicationContext(), "请输入密码", 0).show();
            return;

        } else if (TextUtils.isEmpty(pswAgain)) {
            Toast.makeText(getApplicationContext(), "请再次输入密码", 0).show();
            return;
        } else if (!psw.equals(pswAgain)) {
            Toast.makeText(getApplicationContext(), "两次输入的密码不一样", 0).show();
            return;
        } else if (DBUtils.getInstance(getApplicationContext()).getUserInfo(userName) != null) {
            Toast.makeText(getApplicationContext(), "此账号已经存在", 0).show();
            return;

        } else {
            Toast.makeText(getApplicationContext(), "注册成功", 0).show();
            // 把注册的信息保存在sp中
            SpInfo.SaveInfo(getApplicationContext(), userName, psw, false);
            // 把用户注册的信息保存在数据库里，没有写的字段写默认的
            SaveUserInfoToDb(userName, psw);
            // 把用户名和密码传递到登录页面
            Intent data = new Intent();
            data.putExtra("usename", userName);
            setResult(1, data);
            RegisterActivity.this.finish();
        }
    }

    // 注册成功后，把用户数据保存在数据库
    private void SaveUserInfoToDb(String name, String pwd) {
        // 先将图片加载到应用程序中
        iconFile(this);
        UserBean bean = new UserBean();
        bean.setUserName(name);
        bean.setNickName("精灵");
        bean.setSex("男");
        bean.setSignature("精灵");
        bean.setPwd(MD5Utils.md5Utils(pwd));
        bean.setImages(getFilesDir().getPath() + File.separator + "delault.jpg");
        // 保存用户信息到数据库
        DBUtils.getInstance(this).saveUserInfo(bean);
    }


    // 保存用户信息的时候，有一张默认图片，首先需要将默认图片存放在程序中
    // 创建默认文件夹，并把默认头像放入到文件夹中
    public void iconFile(Context context) {
        String path = context.getFilesDir().getPath();
        //Toast.makeText(context, path, 0).show();
        File file = new File(path, "delault.jpg");
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.defaultuser_icon);
        FileOutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*
     * 获取控件中的字符串
     */
    private void getEditString() {
        userName = etUserName.getText().toString().trim();
        psw = etPsw.getText().toString().trim();
        pswAgain = etPswAgain.getText().toString().trim();
    }


}
