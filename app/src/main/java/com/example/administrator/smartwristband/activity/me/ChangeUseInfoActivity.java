package com.example.administrator.smartwristband.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChangeUseInfoActivity extends BaseActivity {

    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    @BindView(R.id.title_bar)
    RelativeLayout titleBar;
    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.iv_delete)
    ImageView ivDelete;
    @BindView(R.id.tv_mess)
    TextView tvMess;
    private String title, content;
    private int flag;// flag为1时表示修改昵称，为2时表示修改签名,为3表示修改电话

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_use_info);
        initSystemBar(true);
        ButterKnife.bind(this);
        tvSave.setVisibility(View.VISIBLE);
        // 进行初始化
        init();
    }

    private void init() {
        // 获取从个人资料界面传递过来的标题和签名
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        flag = getIntent().getIntExtra("flag", 0);
        System.out.println(flag);
        if (flag == 1) {
            tvMess.setVisibility(View.VISIBLE);
            etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        } else if (flag == 2) {
            tvMess.setText("  最多只能输入30个字符");
            tvMess.setVisibility(View.VISIBLE);
            // 设置为最大字符
            etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        } else if (flag == 3) {
            tvMess.setText("  最多只能输入11个字符");
            tvMess.setVisibility(View.VISIBLE);
            // 设置为最大字符
            etContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        }

        // 如果获取到的数据不为空
        if (!TextUtils.isEmpty(content)) {
            etContent.setText(content);
            etContent.setSelection(content.length());
        }
        contentListener();
        // 点击退后按钮
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeUseInfoActivity.this.finish();
            }
        });
        // 点击删除按钮
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etContent.setText("");
            }
        });
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dataintent = new Intent();
                String enterContent = etContent.getText().toString().trim();
                switch (flag) {
                    case 1:
                        if (!TextUtils.isEmpty(enterContent)) {
                            dataintent.putExtra("nickName", enterContent);
                            setResult(RESULT_OK, dataintent);
                            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                            ChangeUseInfoActivity.this.finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "昵称不能为空", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case 2:
                        if (!TextUtils.isEmpty(enterContent)) {
                            dataintent.putExtra("signture", enterContent);
                            setResult(RESULT_OK, dataintent);
                            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                            ChangeUseInfoActivity.this.finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "签名不能为空", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if (!TextUtils.isEmpty(enterContent)) {
                            dataintent.putExtra("phone", enterContent);
                            setResult(RESULT_OK, dataintent);
                            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                            ChangeUseInfoActivity.this.finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "电话不能为空", Toast.LENGTH_SHORT).show();
                        }

                    default:
                        break;

                }
            }
        });

    }

    /*
     * 监听输入的文字是否在限定范围内
     */
    private void contentListener() {
        // TODO Auto-generated method stub
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                Editable editable = etContent.getText();
                int len = editable.length();
                if (len > 0) {
                    ivDelete.setVisibility(View.VISIBLE);
                } else {
                    ivDelete.setVisibility(View.GONE);
                }
                switch (flag) {
                    case 1:
                        tvMess.setText("  还可以输入" + (8 - len) + "个字符");
                        break;
                    case 2:
                        tvMess.setText("  还可以输入" + (30 - len) + "个字符");
                        break;
                    case 3:
                        tvMess.setText("  还可以输入" + (11 - len) + "个数字");
                    default:
                        break;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }
}
