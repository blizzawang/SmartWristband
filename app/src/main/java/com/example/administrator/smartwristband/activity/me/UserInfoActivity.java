package com.example.administrator.smartwristband.activity.me;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.activity.BaseActivity;
import com.example.administrator.smartwristband.bean.UserBean;
import com.example.administrator.smartwristband.sqlite.DBUtils;
import com.example.administrator.smartwristband.utils.PhotoPopupWindow;
import com.example.administrator.smartwristband.utils.SpInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener {


    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private String spUserName;
    private String path;
    private View rootview;
    private PhotoPopupWindow mPhotoPopupWindow;


    private static final int CHANGE_NICKNAME = 4;// 修改昵称的自定义常量
    private static final int CHANGE_SIGNATURE = 5;// 修改个性签名的自定义常量
    private static final int CHANGE_PHONE = 6;// 修改电话的自定义常量
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private static final String IMAGE_FILE_NAME = "icon.jpg";

    @BindView(R.id.iv_head_icon)
    ImageView ivHeadIcon;
    @BindView(R.id.rl_head)
    RelativeLayout rlHead;
    @BindView(R.id.tv_user_name)
    TextView tvUserName;
    @BindView(R.id.rl_account)
    RelativeLayout rlAccount;
    @BindView(R.id.tv_nickName)
    TextView tvNickName;
    @BindView(R.id.rl_nickName)
    RelativeLayout rlNickName;
    @BindView(R.id.tv_sex)
    TextView tvSex;
    @BindView(R.id.rl_sex)
    RelativeLayout rlSex;
    @BindView(R.id.tv_phone)
    TextView tvPhone;
    @BindView(R.id.rl_phone)
    RelativeLayout rlPhone;
    @BindView(R.id.tv_sign)
    TextView tvSign;
    @BindView(R.id.tv_signature)
    TextView tvSignature;
    @BindView(R.id.rl_signature)
    RelativeLayout rlSignature;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initSystemBar(true);
        ButterKnife.bind(this);
        tvMainTitle.setText("个人信息");
        tvSave.setVisibility(View.GONE);
        if (SpInfo.readLoginStatus(getApplicationContext())) {
            Map<String, String> maps = SpInfo.getInfo(getApplicationContext());
            spUserName = maps.get("username");
            setValue(DBUtils.getInstance(getApplicationContext()).getUserInfo(spUserName));
            setListener();
            verifyStoragePermissions(UserInfoActivity.this);
        }
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfoActivity.this.finish();
            }
        });
    }

    /**
     * 为界面控件设置值
     */
    private void setValue(UserBean bean) {
        tvNickName.setText(bean.getNickName());
        tvUserName.setText(bean.getUserName());
        tvSex.setText(bean.getSex());
        tvPhone.setText(bean.getPhone());
        // 首次加载 如果超过了19个字符，就让内容左对齐
        if (bean.getSignature().length() > 19) {
            tvSignature.setGravity(Gravity.LEFT);
        }
        tvSignature.setText(bean.getSignature());
        ivHeadIcon.setImageBitmap(BitmapFactory.decodeFile(bean.getImages()));
    }

    private void setListener() {
        rlNickName.setOnClickListener(this);
        rlSex.setOnClickListener(this);
        rlPhone.setOnClickListener(this);
        rlSignature.setOnClickListener(this);
        rlHead.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.rl_head:
                // 点击修改头像
                selectImage(UserInfoActivity.this);
                break;

            case R.id.rl_nickName:
                // 点击修改昵称
                // 首先获取到昵称控件上的数据
                String name = tvNickName.getText().toString();
                Intent intentname = new Intent(this, ChangeUseInfoActivity.class);
                intentname.putExtra("content", name);
                intentname.putExtra("title", "昵称");
                intentname.putExtra("flag", 1);
                startActivityForResult(intentname, CHANGE_NICKNAME);
                break;

            case R.id.rl_sex:
                // 点击修改性别
                String sex = tvSex.getText().toString();
                sexDialog(sex);
                break;

            case R.id.rl_phone:
                String phone = tvPhone.getText().toString();
                Intent phoneintent = new Intent(this, ChangeUseInfoActivity.class);
                phoneintent.putExtra("content", phone);
                phoneintent.putExtra("title", "电话");
                phoneintent.putExtra("flag", 3);
                startActivityForResult(phoneintent, CHANGE_PHONE);
                break;

            case R.id.rl_signature:
                // 点击修改签名
                String signature = tvSignature.getText().toString();
                Intent signintent = new Intent(this, ChangeUseInfoActivity.class);
                signintent.putExtra("content", signature);
                signintent.putExtra("title", "签名");
                signintent.putExtra("flag", 2);
                startActivityForResult(signintent, CHANGE_SIGNATURE);
                break;

            default:
                break;
        }
    }

    // 设置性别的弹出框
    public void sexDialog(String sex) {
        int sexFlag = 0;
        if ("男".equals(sex)) {
            sexFlag = 0;
        } else if ("女".equals(sex)) {
            sexFlag = 1;
        }
        final String items[] = {"男", "女"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(items, sexFlag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                tvSex.setText(items[which]);
                // 更新数据库
                DBUtils.getInstance(getApplicationContext()).updateUserInfo("sex", items[which], spUserName);
            }
        });
        builder.create().show();
    }

    // 设置修改头像的弹出框
    private void selectImage(Activity activity) {
        mPhotoPopupWindow = new PhotoPopupWindow(activity, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 文件权限申请
                // 进入相册选择
                mPhotoPopupWindow.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                // 判断系统中是否有处理该 Intent 的 Activity
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_GET);
                } else {
                    Toast.makeText(getApplicationContext(), "未找到图片查看器", Toast.LENGTH_SHORT).show();
                }

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 拍照及文件权限申请
                // 拍照--即调用系统的拍照功能
                mPhotoPopupWindow.dismiss();
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // 指定照片保存路径（SD卡），IMAGE_FILE_NAME所代表的为一个临时文件，每次拍照后这个图片都会被替换
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(getApplicationContext(), "无SD卡", Toast.LENGTH_LONG).show();
                }
            }
        });
        // 先找到父视图
        rootview = LayoutInflater.from(activity).inflate(R.layout.activity_user_info, null);
        mPhotoPopupWindow.showAtLocation(rootview, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        // 回调成功
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // 展示图片
                case REQUEST_BIG_IMAGE_CUTTING:
                    if (data != null) {
                        setBigPicToView();
                    }
                    break;

                // 相册选取
                case REQUEST_IMAGE_GET:
                    try {
                        startBigPhotoZoom(data.getData());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;

                // 拍照
                case REQUEST_IMAGE_CAPTURE:
                    File temp = new File(Environment.getExternalStorageDirectory().getPath(), IMAGE_FILE_NAME);
                    startBigPhotoZoom(Uri.fromFile(temp));
                    break;

                // 修改昵称
                case CHANGE_NICKNAME:
                    if (data != null) {
                        String newNick = data.getStringExtra("nickName");
                        if (TextUtils.isEmpty(newNick)) {
                            return;
                        }
                        tvNickName.setText(newNick);
                        // 更新数据库的昵称信息
                        DBUtils.getInstance(getApplicationContext()).updateUserInfo("nickName", newNick, spUserName);
                    }
                    break;

                // 修改签名
                case CHANGE_SIGNATURE:
                    if (data != null) {
                        String newSign = data.getStringExtra("signture");
                        if (TextUtils.isEmpty(newSign)) {
                            return;
                        }
                        // 簽名如果大於19个字符，就设置为左对齐
                        if (newSign.length() > 19) {
                            tvSignature.setGravity(Gravity.LEFT);
                        } else {
                            tvSignature.setGravity(Gravity.RIGHT);
                        }
                        tvSignature.setText(newSign);
                        // 更新数据库的签名信息
                        DBUtils.getInstance(getApplicationContext()).updateUserInfo("signature", newSign, spUserName);
                    }
                    break;

                //修改电话
                case CHANGE_PHONE:
                    if (data != null) {
                        String newPhone = data.getStringExtra("phone");
                        if (TextUtils.isEmpty(newPhone)) {
                            return;
                        }
                        tvPhone.setGravity(Gravity.RIGHT);
                        tvPhone.setText(newPhone);
                        // 更新数据库的签名信息
                        DBUtils.getInstance(getApplicationContext()).updateUserInfo("phone", newPhone, spUserName);
                    }
            }
        }

    }

    /**
     * 大图模式切割图片 直接创建一个文件将切割后的图片写入--保存在文件中
     */
    Uri imageUri = null;
    private File file;

    public void startBigPhotoZoom(Uri uri) {
        // 创建大图文件夹
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String storage = Environment.getExternalStorageDirectory().getPath();
            File dirFile = new File(storage + "/bigIcon");
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e("TAG", "文件夹创建失败");
                } else {
                    Log.e("TAG", "文件夹创建成功");
                }
            }
            file = new File(dirFile, System.currentTimeMillis() + ".jpg");
            imageUri = Uri.fromFile(file);
        }
        // 开始切割
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1); // 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 600); // 输出图片大小
        intent.putExtra("outputY", 600);
        intent.putExtra("scale", true);
        /**
         * 此方法返回的图片只能是小图片（sumsang测试为高宽160px的图片）
         * 故只保存图片Uri，调用时将Uri转换为Bitmap，此方法还可解决miui系统不能return data的问题
         */
        // intent.putExtra("return-data", true);

        intent.putExtra("return-data", false); // 不直接返回数据
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 返回一个文件
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, REQUEST_BIG_IMAGE_CUTTING);
    }

    /**
     * 大图模式中，保存图片后，设置到视图中 并保存在数据库中
     */
    private void setBigPicToView() {
        Bitmap bm;
        try {
            bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            ivHeadIcon.setImageBitmap(bm);
            DBUtils.getInstance(getApplicationContext()).updateUserInfo("image", file.getPath(), spUserName);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //动态申请相机和读内存卡和写内存卡的权限
    private static final int REQUEST_EXTERNAL_STORAGE = 7;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @OnClick(R.id.tv_back)
    public void onViewClicked() {
    }
}
