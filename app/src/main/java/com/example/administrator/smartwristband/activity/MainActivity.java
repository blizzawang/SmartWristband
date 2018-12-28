package com.example.administrator.smartwristband.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.administrator.smartwristband.R;
import com.example.administrator.smartwristband.fragment.TabDiscoveryFragment;
import com.example.administrator.smartwristband.fragment.TabHomeFragment;
import com.example.administrator.smartwristband.fragment.TabMeFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    String deviceName;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_main_title)
    TextView tvMainTitle;
    @BindView(R.id.tv_save)
    TextView tvSave;
    private MenuItem menuItem;
    private BottomNavigationView navigation;
    private ViewPager viewPager;
    private FragmentPagerAdapter fragmentPagerAdapter;  //适配器
    private List<Fragment> list_fragment;
    private Window window;
    private String[] str = new String[]{"设备", "发现", "我的信息"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initSystemBar(true);
        tvBack.setVisibility(View.GONE);
        tvSave.setVisibility(View.GONE);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //加载fragment;
        initData();
        tvMainTitle.setText(str[0]);


        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //为viewPager设置适配器
        setupViewPager(viewPager);
        //滑动页面触发事件
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                menuItem = navigation.getMenu().getItem(position);
                tvMainTitle.setText(str[position]);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setupViewPager(viewPager);
    }

    private void initData() {
        //加载资源
        list_fragment = new ArrayList<Fragment>();
        Fragment homeFragment = TabHomeFragment.newInstance();
        Fragment discoveryFragment = TabDiscoveryFragment.newInstance();
        Fragment meFragment = TabMeFragment.newInstance();
        list_fragment.add(homeFragment);
        list_fragment.add(discoveryFragment);
        list_fragment.add(meFragment);

    }

    //设置viewPager的适配器
    private void setupViewPager(ViewPager viewPager) {

        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                //返回当前的Fragment
                return list_fragment.get(position);
            }

            @Override
            public int getCount() {
                return list_fragment.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                //super.destroyItem(container, position, object);
            }

        };


        viewPager.setAdapter(fragmentPagerAdapter);
        System.out.println("绑定成功！");
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    tvMainTitle.setText(str[0]);
                    return true;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(1);
                    tvMainTitle.setText(str[1]);
                    return true;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(2);
                    tvMainTitle.setText(str[2]);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            deviceName = data.getStringExtra("name");
        }
    }

    @Override
    protected void onRestart() {
        boolean id = getIntent().getBooleanExtra("isLogin", false);
        if (id) {
            //重新加载TabMeFragment
            viewPager.setCurrentItem(2);
            tvMainTitle.setText(str[2]);
        }
        super.onRestart();

    }

    //设置手机状态栏
    public void initSystemBar(Boolean isLight) {
        if (Build.VERSION.SDK_INT >= 21) {
            //LAYOUT_FULLSCREEN 、LAYOUT_STABLE：让应用的主体内容占用系统状态栏的空间；
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(option);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
            window = getWindow();
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            if (isLight) {
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            } else {
                window.setStatusBarColor(getResources().getColor(R.color.white));
            }

            //状态栏颜色接近于白色，文字图标变成黑色
            View decor = window.getDecorView();
            int ui = decor.getSystemUiVisibility();
            if (isLight) {
                //light --> a|=b的意思就是把a和b按位或然后赋值给a,
                // 按位或的意思就是先把a和b都换成2进制，然后用或操作，相当于a=a|b
                ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                //dark  --> &是位运算里面，与运算,  a&=b相当于 a = a&b,  ~非运算符
                ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decor.setSystemUiVisibility(ui);
        }
    }
}
